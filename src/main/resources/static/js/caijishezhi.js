
$(function () {
    //分页设置
    layui.use(['laypage','layer','element']);
    var laypage = layui.laypage;
    var element = layui.element;
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
    $.ajax({
        url: "/api/listJob",
        type: "get",
        xhrFields: {
            withCredentials: true
        },
        data:{},
        success: function (res) {
            if (res.code != 0) {
                return layer.msg(res.message, {anim: 6});
            }
            $.each(res.data,function (i,item) {
                var list;
                if(item.ifFinish){
                    list = '<li><span id="'+item.taskId+'">'+zdrysc.timechange(item.creationTime)+'</span><a id="'+item.taskId+'" class="btntrue">生成报告</a></li>';
                }else{
                    list = '<li><span id="'+item.taskId+'">'+zdrysc.timechange(item.creationTime)+'</span><a id="'+item.taskId+'" class="btnfalse">生成报告</a></li>';
                }
                $(".sc_renwu").append(list);
            });
            if(res.data.length>0){
                var tafirst = res.data[res.data.length - 1];
                getlist(0,10,tafirst.taskId);
                $(".sc_renwu").find("li").eq(res.data.length - 1).addClass("re_active");
                $(".listpage").attr("data-href",tafirst.taskId,"");
                jindu(tafirst.taskId);
            }
        }
    });
    //点击切换
    $(".sc_renwu").on("click","span",function () {
        $(".sc_renwu").find("li").removeClass("re_active");
        $(this).parent("li").addClass("re_active");
        $(".listpage").attr("data-href",$(this).attr("id"));
        getlist(0,10,$(this).attr("id"),"");
        jindu($(this).attr("id"));
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
                    return layer.msg(res.message, {anim: 6});
                }
                if (0 === pagenum) {
                    count = res.data.data.totalNumber;
                    setPageNo(count,pagesize);
                }
                $.each(res.data.data.dataList,function (i,item) {
                    var appname = '';
                    var appspsn = '';
                    $.each(item.data,function (i,item) {
                        if(i==0){
                            appname+=item.webname
                        }else{
                            appname+="，"+item.webname
                        }
                        appspsn+="<span class='sc_zdgrayspan'>"+item.webtype+"："+item.webname+"</span>";
                    })
                    var list;
                    if(i%2){
                        list = '<div class="sc_zdgray">' +
                            '<span>'+item.mobile+'</span>' +
                            '<span class="cr sc_zdspan" data-href="'+appspsn+'"> '+appname+'</span>' +
                            '<span>'+zdrysc.timechange(item.creationTime)+'</span>' +
                            '</div>';
                    }else{
                        list = '<div>' +
                            '<span>'+item.mobile+'</span>' +
                            '<span class="cr sc_zdspan" data-href="'+appspsn+'">'+appname+'</span>' +
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
        layer.open({
            type: 1,
            shade: false,
            title: false, //不显示标题
            content: $(this).attr("data-href")
        });
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
                console.log(res);
                if (res.code != 0) {
                    return layer.msg(res.message, {anim: 6});
                }
                var total = res.totalCount;
                var suces = res.successCount;
                if(total==suces){
                    setTimeout(function () {
                        element.progress('jindutiao', '100%');
                    },500);
                }else{
                    var jind =  Math.floor(suces/total);

                    element.progress('jindutiao', jind+'%');
                    setTimeout(function () {
                        jindu($(".listpage").attr("data-href"));
                    },3000);
                }
            }
        });
    }
})