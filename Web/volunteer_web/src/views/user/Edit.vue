<template>
  <div class="user-edit-container">
    <div class="page-header">
        <h2>编辑资料</h2>
        <van-button size="small" type="default" @click="router.back()">返回个人中心</van-button>
    </div>
    
    <van-form @submit="onSubmit" class="edit-form">
      <!-- Common Fields -->
      <div class="form-section">
          <h3>个人信息</h3>
          
          <div class="avatar-cell-group">
             <div class="avatar-label">头像</div>
             <div class="avatar-wrapper">
                 <van-uploader v-model="fileList" :max-count="1" :after-read="handleAvatarSelect" :reupload="true" :deletable="false">
                     <template #default>
                         <div class="upload-placeholder">
                             <van-icon name="photograph" size="24" color="#dcdee0" />
                         </div>
                     </template>
                 </van-uploader>
             </div>
             <div class="avatar-hint">点击更换头像</div>
          </div>

          <van-field
            v-model="form.nickname"
            label="昵称"
            placeholder="请输入昵称"
            :rules="[{ required: true, message: '请填写昵称' }]"
          />
      </div>

      <!-- Volunteer Fields -->
      <template v-if="role === 'VOLUNTEER'">
        <div class="form-section">
          <h3>基本信息</h3>
          <van-field 
              v-model="form.realName" 
              label="真实姓名" 
              placeholder="请输入真实姓名" 
              :rules="[{ required: true, message: '请填写真实姓名' }]"
          />
          <van-field 
              v-model="form.studentId" 
              label="学号" 
              placeholder="请输入学号" 
              :rules="[{ required: true, message: '请填写学号' }]"
          />
          <van-field 
              v-model="form.phone" 
              label="联系电话" 
              placeholder="请输入手机号" 
              type="tel" 
              :rules="[
                  { required: true, message: '请填写手机号' },
                  { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
              ]"
          />
          <van-field v-model="form.bio" label="个人简介" type="textarea" rows="3" autosize placeholder="简短介绍一下自己" show-word-limit maxlength="200" />
        </div>
      </template>

      <!-- Organizer Fields -->
      <template v-if="role === 'ORGANIZER'">
        <div class="form-section">
          <h3>组织信息</h3>
          <van-field v-model="form.orgName" label="组织名称" placeholder="请输入组织名称" :rules="[{ required: true, message: '必填' }]" />
          <van-field v-model="form.description" label="组织简介" type="textarea" rows="3" autosize placeholder="介绍您的组织" show-word-limit maxlength="500" />
        </div>
      </template>
      
      <!-- Admin or other logic could go here, but omitted for now -->

      <div class="form-actions">
        <van-button type="primary" native-type="submit" :loading="loading" class="save-btn">
          保存修改
        </van-button>
      </div>
    </van-form>

    <!-- Avatar Cropper Modal -->
    <van-dialog v-model:show="showCropper" title="裁剪头像" show-cancel-button @confirm="confirmCrop" :close-on-click-overlay="false">
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
      <div class="cropper-tip">
        请调整选择头像范围
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getVolunteerProfile, updateVolunteerProfile, getOrganizerProfile, updateOrganizerProfile } from '@/api/user'
import { showSuccessToast, showFailToast, showConfirmDialog, showToast } from 'vant'
import { uploadFile } from '@/api/common'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'

const router = useRouter()
const userStore = useUserStore()
const role = userStore.role || 'VOLUNTEER'
const fileList = ref([])

// Initialize avatar from store if available
const initAvatar = userStore.userInfo.avatar || userStore.userInfo.avatarUrl
if (initAvatar) {
    fileList.value = [{ url: initAvatar, isImage: true }]
}

const form = ref({
    nickname: '',
    avatarUrl: ''
})

const showCropper = ref(false)
const cropperImg = ref('')
const cropper = ref(null)
const tempFile = ref(null)

const handleAvatarSelect = (file) => {
    // Prevent default upload, open cropper instead
    // van-uploader reads file as base64 in file.content
    tempFile.value = file
    cropperImg.value = file.content
    showCropper.value = true
    
    // Clear uploader list for now, we will add manually after crop
    // fileList.value = [] // Don't clear immediately, handled later
}

const confirmCrop = () => {
    // Get cropped blob
    cropper.value.getCropBlob(async (blob) => {
        // Upload blob
        const formData = new FormData()
        // Create a new filename or use existing
        const filename = tempFile.value.file ? tempFile.value.file.name : 'avatar.png'
        formData.append('file', blob, filename)

        try {
            // Set uploading status manually if needed on a placeholder in fileList
            // But since we are in modal, we can show toast
            showToast('上传中...')
            const res = await uploadFile(formData)
            if (res.code === 200) {
                showSuccessToast('上传成功')
                form.value.avatarUrl = res.data
                // Update fileList display
                fileList.value = [{ url: res.data, isImage: true }]
                showCropper.value = false
            } else {
                showFailToast('上传失败: ' + res.message)
            }
        } catch (err) {
            console.error(err)
            showFailToast('上传异常')
        }
    })
}

const afterRead = async (file) => {
    // This is the original logic, now replaced by handleAvatarSelect + confirmCrop
    // Kept just in case or if logic needs reference
    // file.status = 'uploading';
    // ...
}

onMounted(async () => {
  try {
    let res
    if (role === 'VOLUNTEER') {
      res = await getVolunteerProfile()
    } else if (role === 'ORGANIZER') {
       res = await getOrganizerProfile()
    }
    
    if (res && res.code === 200 && res.data) {
        form.value = { ...form.value, ...res.data }
        // Ensure we catch either avatarUrl (API) or avatar (store legacy)
        const remoteAvatar = form.value.avatarUrl || form.value.avatar
        if (remoteAvatar) {
            fileList.value = [{ url: remoteAvatar, isImage: true }]
        }
    }
  } catch (e) {
    console.error(e)
  }
})

const onSubmit = async () => {
    // 增加确认对话框
    showConfirmDialog({
        title: '确认保存',
        message: '确定要保存当前的修改吗？',
    }).then(async () => {
        // 用户点击确认后执行保存逻辑
        loading.value = true
        try {
            let res
            if (role === 'VOLUNTEER') {
              res = await updateVolunteerProfile(form.value)
            } else if (role === 'ORGANIZER') {
              res = await updateOrganizerProfile(form.value)
            } else {
                showFailToast('当前角色不支持修改资料')
                loading.value = false
                return
            }
            
            if (res.code === 200) {
              showSuccessToast('保存成功')
              // Update local store if needed
               userStore.userInfo.avatar = form.value.avatarUrl
               userStore.setUserInfo({ ...userStore.userInfo, avatar: form.value.avatarUrl })

              setTimeout(() => router.back(), 1000)
            } else {
              showFailToast(res.message || '保存失败')
            }
        } catch (error) {
            showFailToast('请求异常')
        } finally {
            loading.value = false
        }
    }).catch(() => {
        // 用户点击取消
    })
}
</script>

<style scoped>
.user-edit-container {
    padding: 20px;
    max-width: 800px;
    margin: 0 auto;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
}
.page-header h2 {
    margin: 0;
    font-size: 24px;
    color: #333;
}
.edit-form {
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.form-section {
    margin-bottom: 30px;
}
.form-section h3 {
    margin-bottom: 20px;
    padding-left: 10px;
    border-left: 4px solid #1989fa;
    font-size: 18px;
    color: #333;
}
.form-actions {
    margin-top: 40px;
    display: flex;
    justify-content: center;
}
.save-btn {
    width: 200px;
}

.cropper-wrapper {
  height: 400px;
  width: 100%;
}
:deep(.cropper-view-box), 
:deep(.cropper-face) {
    border-radius: 50%;
}
.cropper-tip {
    text-align: center;
    color: #999;
    margin-top: 10px;
    font-size: 12px;
}

/* Avatar Styling */
.avatar-cell-group {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    margin-bottom: 20px;
    background: #f9f9f9;
    border-radius: 8px;
}
.avatar-label {
    font-size: 14px;
    color: #646566;
    margin-bottom: 12px;
}
.avatar-wrapper {
    position: relative;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    /* border: 2px solid #fff; */
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    background-color: #f2f3f5;
    overflow: hidden;
}

.upload-placeholder {
    width: 80px;
    height: 80px;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f7f8fa;
    border-radius: 50%;
}
.avatar-hint {
    font-size: 12px;
    color: #969799;
    margin-top: 8px;
}
:deep(.van-uploader) {
    width: 100%;
    height: 100%;
    display: block;
}
:deep(.van-uploader__wrapper) {
    width: 100%;
    height: 100%;
    display: block;
}
:deep(.van-uploader__preview) {
    margin: 0;
    width: 100%;
    height: 100%;
}
:deep(.van-uploader__preview-image), 
:deep(.van-uploader__upload) {
    width: 100% !important;
    height: 100% !important;
    margin: 0;
    border-radius: 50% !important;
    background-color: #f7f8fa; /* Ensure light background if image fails */
    overflow: hidden;
}
:deep(.van-image__img) {
    object-fit: cover !important;
}
:deep(.van-uploader__upload-icon) {
    font-size: 24px;
    color: #dcdee0;
}
</style>

