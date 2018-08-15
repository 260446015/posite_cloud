

$(function () {

    //舆情信息

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
                "赌博","贷款","色情","黄色","主播","直播","游戏","北京"
            ],
            "pageNum": 0,
            "pageSize": 10
        }),
        contentType:"application/json",
        success: function (res) {
            console.log(res);
            if (res.code != 0) {
                return layer.msg(res.message, {anim: 6});
            }
        }
    });


});