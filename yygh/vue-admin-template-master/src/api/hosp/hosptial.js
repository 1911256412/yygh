import request from '@/utils/request'

export default {
    //分页查询医院列表 
    getPageList(current, limit, searchObj) {
        return request({
            url: `/admin/hosp/hospital/selectPage/${current}/${limit}`,
            method: 'get',
            data: searchObj
        })
    },
    //根据dictCode获取下级节点
    findByDictCode(dictCode) {
        return request({
            url: `/admin/cmn/dict/findByDictCode/${dictCode}`,
            method: 'get',
        })
    },
    //根据id查询数据列表
    findByParentId(id) {
        return request({
            url: `/admin/cmn/dict/findChildData/${id}`,
            method: 'get',
        })
    },
    update(id, status) {
        return request({
            url: `/admin/hosp/hospital/update/${id}/${status}`,
            method: 'post',
        })
    },
    //查看医院详情
    getHospById(id) {
        return request({
            url: `/admin/hosp/hospital/show/${id}`,
            method: 'get',
        })
    },
    //查看排班
    getDeptByHoscode(hoscode) {
        return request({
            url: `/admin/hosp/department/getDeparment/${hoscode}`,
            method: 'get',
        })
    }, //查询排班详情
    getScheduleRule(current, limit, hoscode, depcode) {
      return request({
       url: `/admin/hosp/schedule/getSchedule/${current}/${limit}/${hoscode}/${depcode}`,
       method: 'get'
      })
    },
      //查询排班详情
  getScheduleDetail(hoscode,depcode,workDate) {
    return request ({
      url: `/admin/hosp/schedule/getScheduleDetail/${hoscode}/${depcode}/${workDate}`,
      method: 'get'
    })
  }



}