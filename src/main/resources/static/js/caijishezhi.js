
$(function () {
    //分页设置
    layui.use(['laypage','layer','element','form']);
    var laypage = layui.laypage;
    var element = layui.element;
    var form = layui.form;
    //任务创建时间显示
    function timeshow() {
        setTimeout(function () {
            var layerclose;
            $(".treeul .oneli").mouseenter(function () {
                layerclose = layer.tips('任务创建时间：'+$(this).find('label').attr('data-time'),$(this));
            }).mouseleave(function () {
                layer.close(layerclose);
            });
        },1000);
    }
    //分页
    function setPageNo(count,limitnum) {
        laypage.render({
            elem: 'listpage'
            ,count: count
            ,limit:limitnum
            ,layout: ['count', 'prev', 'page', 'next', 'limit', 'skip']
            ,jump: function(obj,first){
                zdrysc.setpagecss();
                if (!first) {
                    getlist((obj.curr - 1),obj.limit,$(".listpage").attr("data-href"),"");
                }
            }
        });
    }
    //三级菜单
    function refreshcaidan() {
        $(".tree label").click(function(event) {
            var $this = $(this).parent();
            var $ul = $this.parent().children("ul");

            if ($ul.length > 0) {
                if($ul.is(":visible")){
                    $ul.slideUp();
                    $this.children("i").removeClass("fa-minus").addClass("fa-plus");
                } else {
                    $ul.slideDown();
                    $this.children("i").removeClass("fa-plus").addClass("fa-minus");
                }
            }
        });
    }
    //任务列表
    getrenwu();
    function getrenwu() {
        $(".treeul").empty();
        $.ajax({
            url: "/api/listJob",
            type: "get",
            xhrFields: {
                withCredentials: true
            },
            data:{},
            success: function (res) {
                console.log(res)
                if (res.code != 0) {
                    //return layer.msg(res.message, {anim: 6});
                }
                if(res.data.length==0){
                    $(".treeul,.im_contenlist").append('<h3 class="kongbai"><img src="../img/zanwushuju.png" alt=""></h3>');
                }
                //开始一级user
                $.each(res.data.user,function (i,item) {
                    //console.log(item)
                    var oli = '<li>' +
                        '<span class="checkspan"><input type="checkbox" name="deletespan" lay-skin="primary" id="'+item.userid+'"><label>'+item.username+'</label></span>' +
                        '<ul style="display: none;">'
                    $.each(item.job,function (i,item) {
                        //console.log(item)
                        //一级下li
                        var otext = "";
                        if(item.taskname){
                            otext = item.taskname;
                        }else{
                            otext = zdrysc.timechange(item.creationTime);
                        }
                        if(item.ifFinish){
                            oli += '<li class="oneli"><input id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><label id="'+item.taskId+'" data-code="'+item.ifFinish+'" data-href="'+item.reportStatus+'" data-time="'+item.creationTime+'">'+otext+'</label></li>';
                        }else{
                            oli += '<li class="oneli"><input id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><label data-href="'+item.reportStatus+'" data-code="'+item.ifFinish+'" id="'+item.taskId+'" data-time="'+item.creationTime+'">'+otext+'</label></li>';
                        }
                    });
                    //一级下用户
                    $.each(item.user,function (i,item) {
                        oli += '<li>' +
                            '<span class="checkspan"><input type="checkbox" name="deletespan" lay-skin="primary" id="'+item.userid+'"><label>'+item.username+'</label></span>' +
                            '<ul style="display: none;">'
                        //二级下li
                        $.each(item.job,function (i,item) {
                            var otext = "";
                            if(item.taskname){
                                otext = item.taskname;
                            }else{
                                otext = zdrysc.timechange(item.creationTime);
                            }
                            if(item.ifFinish){
                                oli += '<li class="oneli"><input id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><label id="'+item.taskId+'" data-code="'+item.ifFinish+'" data-href="'+item.reportStatus+'" data-time="'+item.creationTime+'">'+otext+'</label></li>';
                            }else{
                                oli += '<li class="oneli"><input id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><label data-href="'+item.reportStatus+'" data-code="'+item.ifFinish+'" id="'+item.taskId+'" data-time="'+item.creationTime+'">'+otext+'</label></li>';
                            }
                        });
                        oli += '</ul>' +
                            '</li>'
                    });
                    oli += '</ul>' +
                        '</li>'
                    $(".treeul").append(oli);
                })
                //开始一级列表
                $.each(res.data.job,function (i,item) {
                    var list;
                    var otext = "";
                    if(item.taskname){
                        otext = item.taskname;
                    }else{
                        otext = zdrysc.timechange(item.creationTime);
                    }
                    if(item.ifFinish){
                        list = '<li class="oneli"><input id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><label id="'+item.taskId+'" data-code="'+item.ifFinish+'" data-href="'+item.reportStatus+'" data-time="'+item.creationTime+'">'+otext+'</label></li>';
                    }else{
                        list = '<li class="oneli"><input id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><label data-href="'+item.reportStatus+'" data-code="'+item.ifFinish+'" id="'+item.taskId+'" data-time="'+item.creationTime+'">'+otext+'</label></li>';
                    }
                    $(".treeul").append(list);
                });
                //树形菜单业务
                refreshcaidan();
                //时间显示刷新
                timeshow();
                form.render();
                if(res.data.job.length>0){
                    var tafirst = res.data.job[res.data.job.length - 1];
                    getlist(0,10,tafirst.taskId);
                    $(".treeul").find("li").eq($(".treeul").find("li").length - 1).addClass("re_active");
                    $(".listpage").attr("data-href",tafirst.taskId,"");
                    jindu(tafirst.taskId);
                    $(".re_history").attr("data-href",tafirst.taskId);
                    if(tafirst.ifFinish){
                        $(".caiji_down").fadeIn();
                    }else{
                        $(".caiji_up").fadeIn();
                    }
                    if(tafirst.reportStatus){
                        $(".re_history").fadeIn();
                    }else{
                        $(".re_history").hide();
                    }
                }
            }
        });
    }
    //点击切换
    $(".treeul").on("click",".oneli label",function () {
        var _this = $(this);
        $(".im_contenlist").empty();
        $(".treeul").find("li").removeClass("re_active");
        _this.parent("li").addClass("re_active");
        $(".listpage").attr("data-href",_this.attr("id"));
        getlist(0,10,_this.attr("id"),"");
        $(".re_history").attr("data-href",_this.attr("id"));
        jindu(_this.attr("id"));
        $(".im_btnbox").find(".none").hide();
        if(_this.attr("data-code")=="true"){
            $(".caiji_down").fadeIn();
        }else{
            $(".caiji_up").fadeIn();
        }
        if(_this.attr("data-href")=="true"){
            $(".re_history").fadeIn();
        }else{
            $(".re_history").hide();
        }
    });
    //结果列表
    function getlist(pagenum,pagesize,taskid,msg) {
        $(".im_contenlist").empty();
        $.ajax({
            url: "/api/searchByTaskid",
            type: "get",
            xhrFields: {
                withCredentials: true
            },
            data:{
                taskId: taskid,
                pageNum:pagenum,
                pageSize:pagesize,
                msg:msg
            },
            success: function (res) {
                if (res.code != 0) {
                    //return layer.msg(res.message, {anim: 6});
                }
                if (0 === pagenum) {
                    count = res.data.data.totalNumber;
                    setPageNo(count,pagesize);
                }
                if(res.data.data.dataList.length==0){
                    $(".im_contenlist").append('<h3 class="kongbai"><img src="../img/zanwushuju.png" alt=""></h3>');
                }
                $.each(res.data.data.dataList,function (i,item) {
                    var appname = '';
                    var appspsn = '';
                    var odata;
                    $.each(item.data,function (i,item) {
                        if(i==0){
                            appname+=item.webname
                        }else{
                            appname+="，"+item.webname
                        }
                        appspsn+="<span class='sc_zdgrayspan'>"+item.webtype+"："+item.webname+"</span>";
                    });
                    if(item.data==null){
                        odata = "采集中..."
                    }else{
                        odata = appname;
                    }
                    var list;
                    if(i%2){
                        list = '<div class="sc_zdgray">' +
                            '<span>'+item.mobile+'</span>' +
                            '<span class="cr sc_zdspan" data-href="'+appspsn+'"> '+odata+'</span>' +
                            '<span>'+zdrysc.timechange(item.creationTime)+'</span>' +
                            '</div>';
                    }else{
                        list = '<div>' +
                            '<span>'+item.mobile+'</span>' +
                            '<span class="cr sc_zdspan" data-href="'+appspsn+'">'+odata+'</span>' +
                            '<span>'+zdrysc.timechange(item.creationTime)+'</span>' +
                            '</div>';
                    }
                    $(".im_contenlist").append(list);
                });
            }
        });
    }
    //点击详情
    $(".im_contenlist").on("click",".sc_zdspan",function () {
        if($(this).attr("data-href")){
            layer.open({
                type: 1,
                shade: false,
                title: false, //不显示标题
                content: $(this).attr("data-href")
            });
        }
    });
    //查询点击
    $(".btnchaxun").click(function () {
        getlist(0,10,$(".listpage").attr("data-href"),$(".numben").val());
    });
    //当亲任务进度条
    function jindu(taskid) {
        $.ajax({
            url: "/api/searchByTaskidPlan",
            type: "get",
            xhrFields: {
                withCredentials: true
            },
            data: {
                taskId: taskid,
            },
            success: function (res) {
                //console.log(res);
                if (res.code != 0) {
                    //return layer.msg(res.message, {anim: 6});
                }
                var total = res.data.totalCount;
                var suces = res.data.successCount;
                if(total==suces){
                    setTimeout(function () {
                        element.progress('jindutiao', '100%');
                        $(".jindushow").find(".layui-progress-text").html('100%');
                    },500);
                }else{
                    var jind =  Math.floor((suces/total)*100);
                    setTimeout(function () {
                        element.progress('jindutiao',jind+'%');
                    },500);
                    $(".jindushow").find(".layui-progress-text").html(jind+'%');
                    setTimeout(function () {
                        jindu($(".listpage").attr("data-href"));
                    },3000);
                }
            }
        });
    }
    //生成任务报告
    $(".treeul").on("click",".btntrue",function () {
        window.location = "repotr_renwu.html?"+$(this).attr("id");
    });
    //开启关闭任务
    function renwustatus(statue) {
        $.ajax({
            url: "/api/updateJob",
            type: "post",
            xhrFields: {
                withCredentials: true
            },
            data: JSON.stringify({
                status: statue,
                taskid: $(".listpage").attr("data-href")
            }),
            contentType: "application/json",
            success: function (res) {
                console.log(res);
                if (res.code != 0) {
                    //return layer.msg(res.message, {anim: 6});
                }
                layer.msg("操作完成");
                setTimeout(function () {
                    location.reload();
                },2000);
            }
        })
    }
    //任务暂停
    $(".re_close").click(function () {
        renwustatus("stop");
    });
    //任务开启
    $(".re_begin,.re_reload").click(function () {
        renwustatus("start");
    });
    //批量删除操作
    $(".piliangdelete").click(function () {
        layer.confirm('确定要删除选中的任务吗？', {
            skin: 'layui-layer-lan', //样式类名
            btn: ['确定','取消'] //按钮
        }, function(){
            piliangshanchu();
        }, function(){
            layer.closeAll()
        });
    });
    //是否全选
    form.on('checkbox(quanxuan)', function(data){
        if(data.elem.checked){
            $("input[name='deleteli']").prop('checked',true);
        }else{
            $("input[name='deleteli']").prop('checked',false);
        }
        form.render();
    });
    //批量删除方法
    function piliangshanchu() {
        var olist = [];
        var alist = [];
        $(".treeul").find(".oneli>.layui-form-checkbox").each(function (i,item) {
            var oid = $(this).siblings("input").attr("id");
            var userid = $(this).parent().parent().siblings("span").find("input").attr("id");
            if(item.getAttribute("class")=="layui-unselect layui-form-checkbox layui-form-checked"){
                olist.push(oid);
                alist.push({"userid":userid,"reneuid":oid})
            }
        });
        if(olist.length == 0){
            return layer.msg("请标选您要删除的任务");
        }
        console.log(alist)
        console.log(olist)
        // $.ajax({
        //     url: "/api/deleteBatch",
        //     type: "get",
        //     xhrFields: {
        //         withCredentials: true
        //     },
        //     data: {
        //         ids:olist
        //     },
        //     success: function (res) {
        //         if (res.code != 0) {
        //             //return layer.msg(res.message, {anim: 6});
        //         }
        //         getrenwu();
        //         layer.closeAll();
        //         layer.msg("删除成功");
        //     }
        // });
    }
    //历史报告跳转
    $(".re_history").click(function () {
        window.location = "historyreport.html?"+$(this).attr("data-href");
    });
    //一件收起放开
    $(".shoufang").click(function () {
        if($(this).attr("data-href")=="true"){
            $(".treeul").find("ul").slideDown();
            $(this).attr({
                "data-href":false,
                "class":"shoufang iconfont icon-shouqi"
            });
        }else{
            $(".treeul").find("ul").slideUp();
            $(this).attr({
                "data-href":true,
                "class":"shoufang iconfont icon-zhankaigengduo"
            });
        }
    });
    //任务指派
    $(".renwuzhipai").click(function () {
        var olist = [];
        var alist = [];
        var clist = [];
        var dlist = [];
        $(".treeul").find(".oneli>.layui-form-checkbox").each(function (i,item) {
            var oid = $(this).siblings("input").attr("id");
            var innerhtml = $(this).siblings("label").html();
            if(item.getAttribute("class")=="layui-unselect layui-form-checkbox layui-form-checked"){
                olist.push(oid);
                clist.push(innerhtml);
            }
        });
        $(".treeul").find(".checkspan>.layui-form-checkbox").each(function (i,item) {
            var oid = $(this).siblings("input").attr("id");
            var innerhtml = $(this).siblings("label").html();
            if(item.getAttribute("class")=="layui-unselect layui-form-checkbox layui-form-checked"){
                alist.push(oid);
                dlist.push(innerhtml);
            }
        });
        if(olist.length == 0){
            return layer.msg('请标选需要被指派任务');
        }
        if(alist.length == 0){
            return layer.msg('请标选需要指派的用户');
        }
        layer.alert('您确定要将任务<span>'+clist+'</span>，指派给用户<span>'+dlist+'</span>吗,操作完成后，用户将可查看被指派的任务；', {
            title:'任务指派',
            id:'zhipai',
            skin: 'layui-layer-lan',
            btn:['确定',"取消"]
            ,closeBtn: 0
            ,anim: 4 //动画类型
            ,btn1:function () {
                rnewuzhipai(olist,alist);
            }
        });
    });
    //指派方法
    function rnewuzhipai(renwulist,userlist) {
        $.ajax({
            url: "/api/assignment",
            type: "post",
            data: {
                taskid:renwulist,
                userid:userlist
            },
            success: function (res) {
                console.log(res);
                if (res.code != 0) {
                    //return layer.msg(res.message, {anim: 6});
                }
                layer.closeAll();
                getrenwu();
            }
        })
    }
    //功能说明
    var settme;
    var lsyerzhipai;
    $(".hovermesg").mouseenter(function () {
        var _this = $(this);
        var time = 0;
        var ohtml = _this.attr("data-hove");
        settme = setInterval(function () {
            time++;
            if(time>8){
                clearInterval(settme);
                lsyerzhipai = layer.tips(ohtml,_this);
            }
        },100);
    }).mouseleave(function () {
        clearInterval(settme);
        setTimeout(function () {
            layer.close(lsyerzhipai);
        },300);
    })
})