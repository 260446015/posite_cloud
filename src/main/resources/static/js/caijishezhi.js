
$(function () {
    //分页设置
    layui.use(['laypage','layer','element','form']);
    var laypage = layui.laypage;
    var element = layui.element;
    var form = layui.form;
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
    //任务列表
    getrenwu();
    function getrenwu() {
        $(".sc_renwu").empty();
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
                    $(".sc_renwu,.im_contenlist").append('<h3 class="kongbai"><img src="../img/zanwushuju.png" alt=""></h3>');
                }
                $.each(res.data,function (i,item) {
                    var list;
                    var otext = "";
                    if(item.taskname){
                        otext = item.taskname;
                    }else{
                        otext = zdrysc.timechange(item.creationTime);
                    }
                    if(item.ifFinish){
                        list = '<li class=""><input id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><span id="'+item.taskId+'">'+otext+'</span><a id="'+item.taskId+'" class="btntrue">生成报告</a></li>';
                    }else{
                        list = '<li><input  id="'+item.taskId+'" type="checkbox" name="deleteli" lay-skin="primary"><span id="'+item.taskId+'">'+otext+'</span><a id="'+item.taskId+'" class="btnfalse">正在采集</a></li>';
                    }
                    $(".sc_renwu").append(list);
                });
                form.render();
                if(res.data.length>0){
                    var tafirst = res.data[res.data.length - 1];
                    getlist(0,10,tafirst.taskId);
                    $(".sc_renwu").find("li").eq(res.data.length - 1).addClass("re_active");
                    $(".listpage").attr("data-href",tafirst.taskId,"");
                    jindu(tafirst.taskId);
                    $(".re_history").attr("data-href",tafirst.taskId);
                    if(tafirst.ifFinish){
                        $(".caiji_down").fadeIn();
                    }else{
                        $(".caiji_up").fadeIn();
                    }
                }
            }
        });
    }
    //点击切换
    $(".sc_renwu").on("click","span",function () {
        $(".im_contenlist").empty();
        $(".sc_renwu").find("li").removeClass("re_active");
        $(this).parent("li").addClass("re_active");
        $(".listpage").attr("data-href",$(this).attr("id"));
        getlist(0,10,$(this).attr("id"),"");
        $(".re_history").attr("data-href",$(this).attr("id"));
        jindu($(this).attr("id"));
        $(".im_btnbox").find(".none").hide();
        if($(this).parent("li").find("a").html()=="生成报告"){
            $(".caiji_down").fadeIn();
        }else{
            $(".caiji_up").fadeIn();
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
    $(".sc_renwu").on("click",".btntrue",function () {
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
        $(".sc_renwu").find(".layui-form-checkbox").each(function (i,item) {
            var oid = $(this).siblings("input").attr("id");
            if(item.getAttribute("class")=="layui-unselect layui-form-checkbox layui-form-checked"){
                olist.push(oid)
            }
        });
        if(olist.length == 0){
            return layer.msg("请标选您要删除的任务");
        }
        //console.log(olist)
        $.ajax({
            url: "/api/deleteBatch",
            type: "get",
            xhrFields: {
                withCredentials: true
            },
            data: {
                ids:olist
            },
            success: function (res) {
                if (res.code != 0) {
                    //return layer.msg(res.message, {anim: 6});
                }
                getrenwu();
                layer.closeAll();
                layer.msg("删除成功");
            }
        });
    }

    //历史报告跳转
    $(".re_history").click(function () {
        window.location = "historyreport.html?"+$(this).attr("data-href");
    });
})