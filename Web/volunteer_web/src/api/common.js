import request from '@/utils/request'

export function uploadFile(data) {
  return request({
    url: '/file/upload',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' } // axios usually handles this, but explicit is okay
  })
}
