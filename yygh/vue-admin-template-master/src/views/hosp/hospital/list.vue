<template>
  <div class="app-container">
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item>
        <el-input v-model="searchObj.hosname" placeholder="医院名称" />
      </el-form-item>
      <el-form-item>
        <el-input v-model="searchObj.hoscode" placeholder="医院编号" />
      </el-form-item>
      <el-button type="primary" icon="el-icon-search" @click="getList()"
        >查询</el-button
      >
    </el-form>
    <!-- 工具条 -->
    <div>
      <el-button type="danger" size="mini" @click="removeRows()"
        >批量删除</el-button
      >
    </div>
    <el-table
      :data="list"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" width="50" label="序号" />
      <el-table-column prop="hosname" label="医院名称" />
      <el-table-column prop="hoscode" label="医院编号" />
      <el-table-column prop="apiUrl" label="api基础路径" width="200" />
      <el-table-column prop="contactsName" label="联系人姓名" />
      <el-table-column prop="contactsPhone" label="联系人手机" />
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          {{ scope.row.status === 1 ? "可用" : "不可用" }}
        </template></el-table-column
      >
      <el-table-column label="操作" width="280" align="center">
        <template slot-scope="scope">
          <el-button
            type="danger"
            size="mini"
            icon="el-icon-delete"
            @click="removeDataById(scope.row.id)"
          >
          </el-button>
        <el-button v-if="scope.row.status==1"
            type="danger"
            size="mini"
            icon="el-icon-error"
            @click="lockHostSet(scope.row.id,0)"
          >锁定</el-button>
        <el-button v-if="scope.row.status==0"
            type="primary"
            size="mini"
            icon="el-icon-success"
            @click="lockHostSet(scope.row.id,1)"
          >取消锁定</el-button>
          <router-link :to="'/hosp/edit/'+scope.row.id">
                <el-button type="primary" size="mini" icon="el-icon-edit"></el-button>
          </router-link>
          

        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="getList"
      :current-page="current"
      style="padding: 30px 0; text-align: center"
      :page-size="limit"
      layout="prev, pager, next, jumper"
      :total="total"
    >
    </el-pagination>
  </div>
</template>
</el-table-column>
</el-table>
</div>

</template>

<script>
import hospitalSetApi from "@/api/hosp/hospitalSet";
export default {
  data() {
    return {
      list: [],
      current: 1,
      limit: 4,
      total: {},
      searchObj: {},
      multipleSelection: [], // 批量选择中选择的记录列表
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 当表格复选框选项发生变化的时候触发
    handleSelectionChange(selection) {
      this.multipleSelection = selection;
    },
    removeRows() {
      this.$confirm("此操作将永久删除该文件, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          var idList = [];
          for (var i = 0; i < this.multipleSelection.length; i++) {
            var obj = this.multipleSelection[i];
            var id = obj.id;
            idList.push(id);
          }
          hospitalSetApi.deleteBatch(idList).then((response) => {
            this.$message({
              type: "success",
              message: "删除成功!",
            });
            this.getList(1);
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
    //锁定和取消锁定
    lockHostSet(id,status){
        hospitalSetApi.lock(id,status).then(response=>{
            this.getList(1);
        })
    },
    // 加载列表数据
    getList(page = 1) {
      // 异步获取远程数据（ajax）
      this.current = page;
      hospitalSetApi
        .getPageList(this.current, this.limit, this.searchObj)
        .then((response) => {
          this.list = response.data.records;
          this.total = response.data.total;
        })
        .catch((error) => {
          console.log("请求失败");
        });
    },
    removeDataById(id) {
      this.$confirm("此操作将永久删除该文件, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          hospitalSetApi.deleteHosp(id).then((response) => {
            this.$message({
              type: "success",
              message: "删除成功!",
            });
            this.getList(1);
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
  },
};
</script>