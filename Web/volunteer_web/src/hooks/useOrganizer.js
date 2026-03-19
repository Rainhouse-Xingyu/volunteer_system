// src/hooks/useOrganizer.js
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import QRCode from 'qrcode'
import { getActivityList, getActivityDetail } from '@/api/activity' // Reuse existing

// Generate token API not in activity.js, define it
import request from '@/utils/request'

export function generateSignToken(activityId) {
    return request({
        url: `/activity/sign-token/${activityId}`,
        method: 'get'
    })
}

export function useGenerateCheckInQR(activityId) {
    const qrCodeUrl = ref('')
    const loading = ref(false)

    const fetchTokenAndGenerateQR = async () => {
        if (!activityId) return
        loading.value = true
        try {
            const res = await generateSignToken(activityId)
            if (res.code === 200) {
                const token = res.data
                const qrData = JSON.stringify({
                    activityId: activityId,
                    signToken: token
                })
                
                QRCode.toDataURL(qrData)
                .then(url => {
                    qrCodeUrl.value = url
                })
                .catch(err => {
                    showToast('生成二维码失败')
                })
            } else {
                showToast(res.message || '获取签到Token失败')
            }
        } catch(err) {
            showToast('请求异常')
        } finally {
            loading.value = false
        }
    }

    return { qrCodeUrl, loading, fetchTokenAndGenerateQR }
}