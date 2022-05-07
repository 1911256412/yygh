import request from '@/utils/request'
// export function login(username, password) {
//     return request({
//       url: '/user/login',
//       method: 'post',
//       data: {
//         username,
//         password
//       }
//     })
//   }

export default{
    getPageList(current ,limit ,searchObj){
        return request({
            url :`/admin/hosp/hospitalSet/findPageHospSet/${current}/${limit}`,
            method: 'post',
            data:searchObj
        })
    },
    deleteHosp(id){
        return request({
            url :`/admin/hosp/hospitalSet/${id}`,
            method: 'delete',
        })
    },
    deleteBatch(idList){
        return request({
            url :`/admin/hosp/hospitalSet/batchRemove`,
            method: 'delete',
            data:idList
        })
    },
    lock(id,status){
        return request({
            url :`/admin/hosp/hospitalSet/lockHospitalSet/${id}/${status}`,
            method: 'put',
        })
    },
      //添加医院设置
  saveHospSet(hospitalSet) {
    return request ({
      url: `/admin/hosp/hospitalSet/saveHospitalSet`,
      method: 'post',
      data: hospitalSet
    })
  },
  //通过id查询医院
  getHosp(id) {
    return request ({
      url: `/admin/hosp/hospitalSet/getHospSet/${id}`,
      method: 'get',
    })
  },
  updateHospital(hospitalSet){
    return request ({
        url: `/admin/hosp/hospitalSet/updateHospitalSet`,
        method: 'post',
        data:hospitalSet
      })
  }

}