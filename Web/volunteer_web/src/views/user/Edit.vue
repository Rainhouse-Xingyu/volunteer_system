<template>
  <div class="user-edit-container">
    <div class="page-header">
        <h2>编辑资料</h2>
        <el-button size="small" @click="router.back()">返回个人中心</el-button>
    </div>
    
    <div class="edit-layout">
        <el-card shadow="hover" class="edit-card">
            <el-form 
                ref="formRef"
                :model="form" 
                :rules="rules"
                label-position="top" 
                class="edit-form"
                @submit.prevent
            >
                <div class="form-section">
                    <h3>个人信息</h3>
                    
                    <div class="avatar-cell-group">
                        <div class="avatar-label">头像</div>
                        <div class="avatar-component-container">
                             <!-- Using el-upload as a trigger only without list -->
                            <el-upload
                                class="avatar-uploader"
                                action="#"
                                :show-file-list="false"
                                :auto-upload="false"
                                :on-change="handleAvatarSelect"
                                accept="image/*"
                            >
                                <div class="avatar-content-wrapper">
                                    <div class="avatar-circle">
                                        <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar-image-cover" />
                                        <div v-else class="upload-placeholder">
                                            <el-icon :size="28" color="#969799"><Camera /></el-icon>
                                        </div>
                                    </div>
                                    <div class="avatar-hint">点击更换头像</div>
                                </div>
                            </el-upload>
                        </div>
                    </div>

                    <el-form-item label="昵称" prop="nickname">
                        <el-input v-model="form.nickname" placeholder="请输入昵称" />
                    </el-form-item>
                </div>

                <!-- Volunteer Fields -->
                <template v-if="role === 'VOLUNTEER'">
                    <div class="form-section">
                        <h3>基本信息</h3>
                        <el-form-item label="真实姓名" prop="realName">
                            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
                        </el-form-item>
                        
                        <el-form-item label="学号" prop="studentId">
                            <el-input v-model="form.studentId" placeholder="请输入学号" />
                        </el-form-item>
                        
                        <el-form-item label="联系电话" prop="phone">
                            <el-input v-model="form.phone" placeholder="请输入手机号" />
                        </el-form-item>
                        
                        <el-form-item label="个人简介">
                             <el-input 
                                v-model="form.bio" 
                                type="textarea" 
                                :rows="3" 
                                placeholder="简短介绍一下自己" 
                                maxlength="200" 
                                show-word-limit 
                            />
                        </el-form-item>
                    </div>
                </template>

                <!-- Organizer Fields -->
                <template v-if="role === 'ORGANIZER'">
                    <div class="form-section">
                        <h3>组织信息</h3>
                        <el-form-item label="组织名称" prop="orgName">
                            <el-input v-model="form.orgName" placeholder="请输入组织名称" />
                        </el-form-item>
                        
                        <el-form-item label="组织简介">
                            <el-input 
                                v-model="form.description" 
                                type="textarea" 
                                :rows="3" 
                                placeholder="介绍您的组织" 
                                maxlength="500" 
                                show-word-limit 
                            />
                        </el-form-item>
                    </div>
                </template>

                <div class="form-actions">
                    <el-button type="primary" :loading="loading" @click="onSubmit" class="save-btn">
                        保存修改
                    </el-button>
                </div>
            </el-form>
        </el-card>
    </div>

    <!-- Avatar Cropper Modal -->
    <el-dialog 
        v-model="showCropper" 
        title="裁剪头像" 
        width="600px" 
        :close-on-click-modal="false"
        append-to-body
    >
      <div class="cropper-wrapper">
        <vue-cropper
          ref="cropper"
          :img="cropperImg"
          :output-size="1"
          :output-type="'png'"
          :info="true"
          :full="true"
          :can-move="true"
          :can-move-box="true"
          :fixed-box="false"
          :original="false"
          :auto-crop="true"
          :auto-crop-width="200"
          :auto-crop-height="200"
          :center-box="true"
          :high="true"
          mode="cover"
          :fixed="true"
          :fixed-number="[1, 1]"
        ></vue-cropper>
      </div>
      <template #footer>
          <span class="dialog-footer">
              <el-button @click="showCropper = false">取消</el-button>
              <el-button type="primary" @click="confirmCrop">确认裁剪</el-button>
          </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getVolunteerProfile, updateVolunteerProfile, getOrganizerProfile, updateOrganizerProfile } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/common'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'

const router = useRouter()
const userStore = useUserStore()
const role = userStore.role || 'VOLUNTEER'
const loading = ref(false)
const formRef = ref(null)

const form = ref({
    nickname: '',
    avatarUrl: '',
    realName: '',
    studentId: '',
    phone: '',
    bio: '',
    orgName: '',
    description: ''
})

const rules = {
    nickname: [{ required: true, message: '请填写昵称', trigger: 'blur' }],
    realName: [{ required: true, message: '请填写真实姓名', trigger: 'blur' }],
    studentId: [{ required: true, message: '请填写学号', trigger: 'blur' }],
    orgName: [{ required: true, message: '请填写组织名称', trigger: 'blur' }],
    phone: [
        { required: true, message: '请填写手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
    ]
}

const showCropper = ref(false)
const cropperImg = ref('')
const cropper = ref(null)
const tempFile = ref(null)

// Initialize avatar from store if available
const initAvatar = userStore.userInfo.avatar || userStore.userInfo.avatarUrl
if (initAvatar) {
    form.value.avatarUrl = initAvatar
}

const handleAvatarSelect = (uploadFile) => {
    // Element Plus upload component returns a file object similar to HTML5 File
    // We need to read it to display in cropper
    const rawFile = uploadFile.raw
    if (!rawFile) return

    tempFile.value = rawFile
    
    // Read file as base64 for cropper
    const reader = new FileReader()
    reader.onload = (e) => {
        cropperImg.value = e.target.result
        showCropper.value = true
    }
    reader.readAsDataURL(rawFile)
}

const confirmCrop = () => {
    // Get cropped blob
    cropper.value.getCropBlob(async (blob) => {
        if (!blob) {
            ElMessage.error('裁剪失败')
            return
        }
        
        // Upload blob
        const formData = new FormData()
        // Create a new filename or use existing
        const filename = tempFile.value ? tempFile.value.name : 'avatar.png'
        formData.append('file', blob, filename)

        try {
            // Can show loading if desired, Element Plus usually doesn't need global toast for this
            
            const res = await uploadFile(formData)
            
            if (res.code === 200) {
                ElMessage.success('头像上传成功')
                form.value.avatarUrl = res.data
                showCropper.value = false
            } else {
                ElMessage.error('上传失败: ' + res.message)
            }
        } catch (err) {
            console.error(err)
            ElMessage.error('上传异常')
        }
    })
}


onMounted(async () => {
  if (role === 'ADMIN') {
    ElMessage.warning('管理员不允许修改个人信息')
    router.replace('/dashboard')
    return
  }
  try {
    let res
    if (role === 'VOLUNTEER') {
      res = await getVolunteerProfile()
    } else if (role === 'ORGANIZER') {
       res = await getOrganizerProfile()
    }
    
    if (res && res.code === 200 && res.data) {
        // Merge data into form
        const data = res.data
        form.value = {
            ...form.value,
            ...data,
            // Ensure avatarUrl is set correctly 
            avatarUrl: data.avatarUrl || data.avatar || form.value.avatarUrl
        }
    }
  } catch (e) {
    console.error(e)
  }
})

const onSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate((valid) => {
        if (valid) {
             ElMessageBox.confirm(
                '确定要保存当前的修改吗？',
                '确认保存',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            ).then(async () => {
                loading.value = true
                try {
                    let res
                    if (role === 'VOLUNTEER') {
                      res = await updateVolunteerProfile(form.value)
                    } else if (role === 'ORGANIZER') {
                      res = await updateOrganizerProfile(form.value)
                    } else {
                        ElMessage.error('当前角色不支持修改资料')
                        loading.value = false
                        return
                    }
                    
                    if (res.code === 200) {
                      ElMessage.success('保存成功')
                      // Update local store if needed
                       userStore.userInfo.avatar = form.value.avatarUrl
                       userStore.setUserInfo({ ...userStore.userInfo, avatar: form.value.avatarUrl })

                      setTimeout(() => router.back(), 1000)
                    } else {
                      ElMessage.error(res.message || '保存失败')
                    }
                } catch (error) {
                    ElMessage.error('请求异常')
                } finally {
                    loading.value = false
                }
            }).catch(() => {
                // User cancelled
            })
        }
    })
}
</script>

<style scoped src="@/styles/user-edit.css"></style>