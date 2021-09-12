<template>
  <div class="app-container" style="width: 700px;text-align: center;margin: 10px auto">
    <el-form label-width="120px" >
      <el-form-item label="医院名称">
        <el-input v-model="hospitalSet.hosname"/>
      </el-form-item>
      <el-form-item label="医院编号">
        <el-input v-model="hospitalSet.hoscode"/>
      </el-form-item>
      <el-form-item label="api基础路径">
        <el-input v-model="hospitalSet.apiUrl"/>
      </el-form-item>
      <el-form-item label="联系人姓名">
        <el-input v-model="hospitalSet.contactsName"/>
      </el-form-item>
      <el-form-item label="联系人手机">
        <el-input v-model="hospitalSet.contactsPhone"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveOrUpdate">保存</el-button>
      </el-form-item>
    </el-form>

  </div>
</template>

<script>
import hospSet from "@/api/hospset";

export default {
  name: "add",
  data() {
    return {
      hospitalSet: {}
    }
  },
  created() {
    if(this.$route.params && this.$route.params.id){
      const id = this.$route.params.id
      this.getHospSet(id)
    }else {
      this.hospitalSet = {}
    }
  },
  methods: {
    getHospSet(id) {
      hospSet.getHospSet(id)
        .then(res => {
          this.hospitalSet = res.data
        })
    },
    save(){
      hospSet.saveHospSet(this.hospitalSet)
        .then(res => {
          this.$message({
            type: 'success',
            message: '添加成功!'
          });
          this.$router.push({path:'/hospSet/list'})
        })
    },
    update(){
      hospSet.updateHospSet(this.hospitalSet)
        .then(res => {
          this.$message({
            type: 'success',
            message: '修改成功!'
          });
          this.$router.push({path:'/hospSet/list'})
        })
    },
    saveOrUpdate(){
      if (!this.hospitalSet.id){
        this.save()
      }else {
        this.update()
      }
    }
  }
}
</script>

<style scoped>

</style>
