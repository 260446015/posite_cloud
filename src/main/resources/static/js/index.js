

$(function () {
    //首页获取百分比
    getzhanbi();
    function getzhanbi() {
        $.ajax({
            url: "/api/retrieveData",
            type: "get",
            xhrFields: {
                withCredentials: true
            },
            data: {},
            success: function (res) {
                //console.log(res.data.percent);
                if (res.code != 0) {
                    return layer.msg(res.message, {anim: 6});
                }
                var s = (res.data.successData)/(res.data.totalCount);
                var H = $(".sc_jindu").height();
                var d = $(".sc_jindu").width();
                var h = Math.sqrt(2*s*H*d);
                $(".sc_jindu2").css("height",h);
                $(".jindunum").html(res.data.percent);
                if(res.data.successData<res.data.totalCount){
                    setTimeout(function () {
                        getzhanbi();
                    },3000);
                }
            }
        });
    }

    //舆情信息
    yuqinghuoqu(0,11);
    function yuqinghuoqu(pagenum,pagesize) {
        //近7天时间
        var time1 = new Date();
        time1.setTime(time1.getTime());
        var Y1 = time1.getFullYear();
        var M1 = ((time1.getMonth() + 1) > 10 ? (time1.getMonth() + 1) : '0' + (time1.getMonth() + 1));
        var D1 = (time1.getDate() > 10 ? time1.getDate() : '0' + time1.getDate());
        var timer1 = Y1 +'-'+ M1 +'-'+ D1+' 00:00:00' ;// 当前时间
        var time2 = new Date();
        time2.setTime(time2.getTime() - (24 * 60 * 60 * 1000 * 7));
        var Y2 = time2.getFullYear();
        var M2 = ((time2.getMonth() + 1) > 9 ? (time2.getMonth() + 1) : '0' + (time2.getMonth() + 1));
        var D2 = (time2.getDate() > 9 ? time2.getDate() : '0' + time2.getDate());
        var timer2 = Y2+'-'+ M2+'-'+ D2+' 00:00:00'; // 之后的七天或者一个月

        $.ajax({
            url: "/api/sentiment",
            type: "post",
            xhrFields: {
                withCredentials: true
            },
            data: JSON.stringify({
                "beginDate": timer2,
                "endDate": timer1,
                "msg": [
                    "赌博","贷款","色情","黄色","主播","直播","游戏","承德"
                ],
                "pageNum": pagenum,
                "pageSize": pagesize
            }),
            contentType:"application/json",
            success: function (res) {
                //console.log(res);
                if (res.code != 0) {
                    return layer.msg(res.message, {anim: 6});
                }
                if(res.data.result.length==0) {
                    $(".list_box").append('<h3 class="kongbai"><img src="../img/zanwushuju.png" alt=""></h3>');
                }
                $.each(res.data.result,function (i,item) {
                    var list;
                    if(i%2){
                        list = "<a target='_blank' href='"+item.url+"'>" +
                            "<i>"+item.cleanTitle+"</i>" +
                            "<span>"+zdrysc.timechange(item.createTime)+"</span>" +
                            "</a>";
                    }else{
                        list = "<a target='_blank' href='"+item.url+"' class='sc_zdgray'>" +
                            "<i>"+item.cleanTitle+"</i>" +
                            "<span>"+zdrysc.timechange(item.createTime)+"</span>" +
                            "</a>";
                    }
                    $(".sc_yuqing").append(list);
                });
            }
        });
    }
    //点击详情
    // $(".sc_yuqing").on("click","div",function () {
    //     layer.open({
    //         type: 1,
    //         shade: false,
    //         area: ["900px","600px"], //宽高
    //         title: false, //不显示标题
    //         content: $(this).find("i").html()
    //     });
    // });

    //获取实时注册情况
    var keynum = 0;
    regsintime();
    function regsintime() {
        $.ajax({
            url: "/api/timeRegist",
            type: "get",
            xhrFields: {
                withCredentials: true
            },
            data: {},
            success: function (res) {
                //console.log(res);
                if (res.code != 0) {
                    return layer.msg(res.message, {anim: 6});
                }
                if(res.data.length==0) {
                    $(".list_box").append('<h3 class="kongbai"><img src="../img/zanwushuju.png" alt=""></h3>');
                }
                $.each(res.data,function (i,item) {
                    if(i%2){
                        list = '<li class="sc_zdgray">' +
                            '<span>'+item.mobile+'</span>' +
                            '<span>'+item.webtype+'</span>' +
                            '<span>'+item.app+'</span>' +
                            '</li>';
                    }else{
                        list = '<li>' +
                            '<span>'+item.mobile+'</span>' +
                            '<span>'+item.webtype+'</span>' +
                            '<span>'+item.app+'</span>' +
                            '</li>';
                    }
                    $(".list_box").append(list);
                });
                if(keynum==0){
                    //滚动设置
                    $('.list_lh li:even').addClass('lieven');
                    $("div.list_lh").myScroll({
                        speed: 30, //数值越大，速度越慢
                        rowHeight: 30 //li的高度
                    });
                }
                keynum++;
            }
        });
        setTimeout(function () {
            regsintime();
        },3600000);
    }

    //图表占比分析
    var dom = document.getElementById("container");
    var myChart = echarts.init(dom);
    var app = {};
    option = null;
    app.title = '环形图';
    option = {
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        legend: {
            orient: 'vertical',
            x: 'left',
            data:['网络赌博','网络贷款','色情网站','网络游戏','网络直播']
        },
        color:['#f1c12e','#a88ee2','#4384f0','#01e0a4','#47bcfb'],
        series: [
            {
                name: '',
                type: 'pie',
                radius: ['40%', '55%'],
                data: []
            }
        ]
    };
    if (option && typeof option === "object") {
        myChart.setOption(option, true);
    }
    $.ajax({
        url: "/api/development",
        type: "get",
        xhrFields: {
            withCredentials: true
        },
        data: {},
        success: function (res) {
            //console.log(res);
            if (res.code != 0) {
                return layer.msg(res.message, {anim: 6});
            }
            var num = res.data;
            myChart.setOption({
                series: [{
                    // 根据名字对应到相应的系列
                    name: '平台数量占比',
                    data: [
                        {value:num.gamble, name:'网络赌博'},
                        {value:num.loans, name:'网络贷款'},
                        {value:num.yellow, name:'色情网站'},
                        {value:num.game, name:'网络游戏'},
                        {value:num.living, name:'网络直播'}
                    ]
                }]
            });
            // 设置加载等待隐藏
            myChart.hideLoading();
        }
    });

    //重点人员危险系数排名
    getimportlist(100,0,"",0,10,"","","");
    function getimportlist(maxSorce,minSorce,mobile,pageNum,pageSize,username,webname,webtype) {
        $(".sc_zdbox").empty();
        $.ajax({
            url: "/api/warning",
            type: "post",
            xhrFields: {
                withCredentials: true
            },
            data: JSON.stringify({
                maxSorce: maxSorce,
                minSorce: minSorce,
                mobile: mobile,
                pageNum: pageNum,
                pageSize: pageSize,
                username: username,
                webname: webname,
                webtype: webtype
            }),
            contentType: "application/json",
            success: function (res) {
                //console.log(res);
                if (res.code != 0) {
                    return layer.msg(res.message, {anim: 6});
                }
                if(!res.data.dataList){
                    $(".sc_zdbox").append('<h3 class="kongbai"><img src="../img/zanwushuju.png" alt=""></h3>');
                }
                $.each(res.data.dataList,function (i,item) {
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
                    if(i%2){
                        list = '<div class="sc_zdgray">' +
                            '<span>Top '+(i+1)+'</span>' +
                            '<span>'+item.mobile+'</span>' +
                            '<span>'+item.registCount+'</span>' +
                            '<span>'+item.sorce+'</span>' +
                            '</div>';
                    }else{
                        list = '<div>' +
                            '<span>Top '+(i+1)+'</span>' +
                            '<span>'+item.mobile+'</span>' +
                            '<span>'+item.registCount+'</span>' +
                            '<span>'+item.sorce+'</span>' +
                            '</div>';
                    }
                    $(".sc_zdbox").append(list);
                });
            }
        });
    }
});