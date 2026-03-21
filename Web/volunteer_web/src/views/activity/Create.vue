<template>
  <div class="create-activity-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>发布新活动</span>
        </div>
      </template>
      
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="120px" 
        size="large"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题"></el-input>
        </el-form-item>

        <el-form-item label="活动分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择活动分类" style="width: 100%;">
            <el-option
              v-for="item in categories"
              :key="item.value"
              :label="item.text"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点"></el-input>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招募人数" prop="quota">
              <el-input-number v-model="form.quota" :min="1" :max="1000" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="奖励积分" prop="rewardPoints">
              <el-input-number v-model="form.rewardPoints" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动详情" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入详细的活动描述、要求等..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">立即发布</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createActivity } from '@/api/activity'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
    title: '',
    category: '',
    startTime: '',
    endTime: '',
    location: '',
    quota: 10,
    rewardPoints: 0,
    content: ''
})

const categories = [
    { text: '社区服务', value: '社区服务' },
    { text: '环境保护', value: '环境保护' },
    { text: '敬老助残', value: '敬老助残' },
    { text: '支教助学', value: '支教助学' },
    { text: '文化传播', value: '文化传播' },
    { text: '赛会服务', value: '赛会服务' },
    { text: '其他', value: '其他' }
]

const rules = {
    title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
    category: [{ required: true, message: '请选择分类', trigger: 'change' }],
    startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
    endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
    location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
    quota: [{ required: true, message: '请输入招募人数', trigger: 'blur' }],
    content: [{ required: true, message: '请输入活动详情', trigger: 'blur' }]
}

const onSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid, fields) => {
        if (valid) {
            loading.value = true
            try {
                const res = await createActivity(form)
                if(res.code === 200) {
                    ElMessage.success('发布成功，等待审核')
                    router.push('/home')
                } else {
                     ElMessage.error(res.message || '发布失败')
                }
            } catch (e) {
                console.error(e)
                ElMessage.error('发布失败')
            } finally {
                loading.value = false
            }
        } else {
            console.log('error submit!', fields)
        }
    })
}

const resetForm = () => {
    if (!formRef.value) return
    formRef.value.resetFields()
}
</script>

<style scoped src="@/styles/activity-create.css"></style>
