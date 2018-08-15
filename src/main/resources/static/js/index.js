

$(function () {
    yuqinghuoqu(0,11);
    //舆情信息
    function yuqinghuoqu(pagenum,pagesize) {
        $.ajax({
            url: "/api/sentiment",
            type: "post",
            xhrFields: {
                withCredentials: true
            },
            data: JSON.stringify({
                "beginDate": "2018-08-00 00:00:00",
                "endDate": "2018-08-15 00:00:00",
                "msg": [
                    "赌博","贷款","色情","黄色","主播","直播","游戏"
                ],
                "pageNum": pagenum,
                "pageSize": pagesize
            }),
            contentType:"application/json",
            success: function (res) {
                console.log(res);
                if (res.code != 0) {
                    return layer.msg(res.message, {anim: 6});
                }
                $.each(res.data.result,function (i,item) {
                    var list;
                    if(i%2){
                        list = "<div data-href='"+item.content+"'>" +
                            "<a>"+item.cleanTitle+"</a>" +
                            "<span>"+zdrysc.timechange(item.createTime)+"</span>" +
                            "</div>";
                    }else{
                        list = "<div class='sc_zdgray'' data-href='"+item.content+"'>" +
                            "<a>"+item.cleanTitle+"</a>" +
                            "<span>"+zdrysc.timechange(item.createTime)+"</span>" +
                            "</div>";
                    }
                    $(".sc_yuqing").append(list);
                });
            }
        });
    }
    //点击详情
    $(".sc_yuqing").on("click","div",function () {
        layer.open({
            type: 1,
            shade: false,
            area: ["900px","600px"], //宽高
            title: false, //不显示标题
            content: $(this).attr("data-href")
        });
    });

});