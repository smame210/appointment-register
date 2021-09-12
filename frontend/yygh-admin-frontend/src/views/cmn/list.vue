<template>
  <div class="app-container">

    <div class="el-toolbar">
      <div class="el-toolbar-body" style="display:flex;justify-content: flex-start;align-items: flex-start;margin: 10px auto">
        <div style="margin: 0 10px">
          <el-button type="primary" @click="exportData">下载<i class="el-icon-download el-icon--right"></i></el-button>
        </div>
        <el-upload
          class="upload-demo"
          action="http://localhost:8202/admin/cmn/dict/importData"
          :multiple="false"
          :on-success="onUploadSuccess"
          :on-error="onUploadError"
          :show-file-list="true">
          <el-button type="primary">上传<i class="el-icon-upload el-icon--right"></i></el-button>
          <div slot="tip" class="el-upload__tip">只能上传xls文件，且不超过500kb</div>
        </el-upload>
      </div>
    </div>

    <el-table
      :data="list"
      style="width: 100%"
      row-key="id"
      border
      lazy
      :load="getChildren"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column label="名称" width="300px" align="left">
        <template slot-scope="scope">
          <span>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="编码" width="220">
        <template slot-scope="{row}">
          {{ row.dictCode }}
        </template>
      </el-table-column>
      <el-table-column label="值" width="230" align="left">
        <template slot-scope="scope">
          <span>{{ scope.row.value }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import cmn from '@/api/cmn'

export default {
  name: "list",
  data() {
    return {
      list: []
    }
  },
  created() {
    this.getDictList(1)
  },
  methods: {
    getDictList(id){
      cmn.getDictList(id)
      .then(res => {
        this.list = res.data
      })
    },
    getChildren(tree, treeNode, resolve) {
      cmn.getDictList(tree.id)
        .then(res => {
          resolve(res.data)
        })
    },
    exportData(){
      window.location.href = 'http://localhost:8202/admin/cmn/dict/exportData'
    },
    onUploadSuccess(response, file, fileList) {
      this.$message({
        type: 'success',
        message: '上传成功!'
      });
      this.getDictList(1)
    },
    onUploadError(err, file, fileList) {
      this.$message.error('上传失败!');
    }
  }
}
</script>

<style scoped>

</style>
