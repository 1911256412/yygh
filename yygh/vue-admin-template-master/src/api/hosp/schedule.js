import request from '@/utils/request'

export default {
      //查询排班详情
      getScheduleRule(current, limit, hoscode, depcode) {
        return request({
         url: `/admin/hosp/schedule/getSchedule/${current}/${limit}/${hoscode}/${depcode}`,
         method: 'get'
        })
}
}