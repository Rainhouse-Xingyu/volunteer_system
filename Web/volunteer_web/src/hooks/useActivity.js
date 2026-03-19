// src/hooks/useActivity.js
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getActivityList, getActivityDetail, registerActivity, cancelRegistration, checkIn } from '@/api/activity'
import { Html5Qrcode } from 'html5-qrcode'

export function useActivityList() {
  const list = ref([])
  const loading = ref(false)
  const networkError = ref(false) 
  const finished = ref(false)
  const page = ref(1)

  const onLoad = async () => {
    loading.value = true
    networkError.value = false
    try {
      const res = await getActivityList(page.value, 10)
      if (res.code === 200) {
        list.value.push(...res.data.records)
        // If current size < 10 or total achieved, finish
        if (res.data.records.length < 10) {
          finished.value = true
        } else {
          page.value++
        }
      }
    } catch(err) {
      networkError.value = true
    } finally {
      loading.value = false
    }
  }

  return { list, loading, finished, onLoad }
}

export function useActivityDetail(id) {
    const detail = ref({})
    const loading = ref(true)

    const fetchDetail = async () => {
        try {
            const res = await getActivityDetail(id)
            if (res.code === 200) {
                detail.value = res.data
            }
        } catch(err) {
            showToast('获取活动详情失败')
        } finally {
            loading.value = false
        }
    }

    const handleRegister = async () => {
        try {
            const res = await registerActivity(id)
            if (res.code === 200) {
                showToast('报名成功')
                fetchDetail() // refresh status
            } else {
                showToast(res.msg || '报名失败')
            }
        } catch(err) {
            showToast('报名请求异常')
        }
    }

    return { detail, loading, fetchDetail, handleRegister }
}

export function useCheckIn() {
  const showScanner = ref(false)
  let html5QrCode = null

  const startScan = () => {
    showScanner.value = true
    html5QrCode = new Html5Qrcode("reader")
    html5QrCode.start(
      { facingMode: "environment" },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      onScanSuccess,
      onScanFailure
    ).catch(err => {
      console.error(err)
      showToast('无法启动相机')
    })
  }

  const stopScan = () => {
    if (html5QrCode) {
        html5QrCode.stop().then(() => {
            html5QrCode.clear()
            showScanner.value = false
        }).catch(err => console.error(err))
    } else {
        showScanner.value = false
    }
  }

  const onScanSuccess = async (decodedText) => {
    // Stop scanning once success
    stopScan()
    
    // Parse QR JSON: { activityId: 1, signToken: "uuid..." }
    try {
        const data = JSON.parse(decodedText)
        if (!data.activityId || !data.signToken) {
            showToast('无效的二维码')
            return
        }

        const res = await checkIn(data)
        if (res.code === 200) {
            showToast('签到成功!')
        } else {
            showToast(res.msg || '签到失败')
        }
    } catch(err) {
        showToast('无效的二维码数据')
    }
  }

  const onScanFailure = (error) => {
    // console.warn(`Scan error = ${error}`)
  }

  return { showScanner, startScan, stopScan }
}