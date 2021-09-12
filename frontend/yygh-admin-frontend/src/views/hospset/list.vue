<template>
  <div class="app-container">
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item>
        <el-input  v-model="searchObj.hosname" placeholder="医院名称"/>
      </el-form-item>
      <el-form-item>
        <el-input v-model="searchObj.hoscode" placeholder="医院编号"/>
      </el-form-item>
      <el-button type="primary" icon="el-icon-search" @click="getList()">查询</el-button>
    </el-form>

    <div>
      <el-button type="danger" size="mini" @click="removeRows()">批量删除</el-button>
    </div>

    <el-table :data="list" stripe style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55"/>
      <el-table-column type="index" label="序号" width="50"/>
      <el-table-column prop="hosname" label="医院名称"/>
      <el-table-column prop="hoscode" label="医院编号"/>
      <el-table-column prop="apiUrl" label="api基础路径"width="200"/>
      <el-table-column prop="contactsName" label="联系人姓名"/>
      <el-table-column prop="contactsPhone" label="联系人手机"/>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          {{ scope.row.status === 1 ? '可用' : '不可用' }}
        </template>
      </el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        width="160">
        <template slot-scope="scope">
          <el-button
            @click.native.prevent="deleteRow(scope.row.id)"
            type="text"
            size="small">
            移除
          </el-button>
          <el-button v-if="scope.row.status==1"
            @click.native.prevent="lockHospSet(scope.row.id, 0)"
            type="text"
            size="small">
            锁定
          </el-button>
          <el-button v-else="scope.row.status==0"
            @click.native.prevent="lockHospSet(scope.row.id, 1)"
            type="text"
            size="small">
            取消锁定
          </el-button>
          <router-link :to="'/hospSet/edit/'+scope.row.id" style="margin-left: 10px">
            <el-button type="text" size="small">
              修改
            </el-button >
          </router-link>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :total="total"
      :current-page="current"
      :page-sizes="[3, 5, 10, 15]"
      :page-size="3"
      layout="total, sizes, prev, pager, next, jumper"
      style="padding: 30px 0; text-align: center;"
      @size-change="handleSizeChange"
      @current-change="getList">
    </el-pagination>
  </div>
</template>

<script>
import hospSet from '@/api/hospset'

export default {
  name: "list",
  data() {
    return {
      current: 1,
      limit: 3,
      total: 0,
      searchObj: {},
      list: [],
      multipleSelection: []
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList(page = 1) {
      this.current = page
      hospSet.getHospSetList(this.current, this.limit, this.searchObj)
        .then(res => {
          console.log(res)
          this.list = res.data.records;
          this.total = res.data.total;
        }).catch(err => {
          console.log(err)
        })
    },
    handleSizeChange(size) {
      this.limit = size
      this.getList()
    },
    deleteRow(id) {
        this.$confirm('此操作将永久删除该医院设置信息, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          hospSet.deleteHospSet(id)
          .then(res => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
            this.getList()
          }).catch(err => {
            this.$message({
              type: 'info',
              message: '删除出错!'
            });
          })
        });
    },
    handleSelectionChange(selected) {
      this.multipleSelection = selected;
    },
    removeRows() {
      this.$confirm('此操作将永久删除该医院设置信息, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const idList = this.multipleSelection.map(val => val.id)
        console.log(idList)
        hospSet.batchRemoveHospSet(idList)
          .then(res => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
            this.getList()
          }).catch(err => {
          this.$message({
            type: 'info',
            message: '删除出错!'
          });
        })
      });
    },
    lockHospSet(id, status) {
      hospSet.lockHospSet(id, status)
      .then(res => {
        this.getList(this.current)
      })
    }
  }
}
</script>

<style scoped>

</style>
