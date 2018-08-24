var h = $(window).height();
var h1 = $("body").height();
var w = $(window).width();
console.log(h + "+" + w)
var zdrysc = {
    base: '中科点击',
    user: 'jianghaife',
    qq: '1465089870'
};
var list;
//时间
var mydata, n, y, r, s, f, m, z, state;
zdrysc.getTime = function () {
    mydata = new Date();
    var n = mydata.getFullYear();
    var y = ((mydata.getMonth() + 1) > 9 ? (mydata.getMonth() + 1) : '0' + (mydata.getMonth() + 1));
    var r =  (mydata.getDate() > 9 ? mydata.getDate() : '0' + mydata.getDate());
    var s = (mydata.getHours() > 9 ? mydata.getHours() : '0' + mydata.getHours());
    var f = (mydata.getMinutes() > 9 ? mydata.getMinutes() : '0' + mydata.getMinutes());
    var m = (mydata.getSeconds() > 9 ? mydata.getSeconds() : '0' + mydata.getSeconds());

    $(".riqi").html(n+"年"+y+"月"+r+"日");

    switch (mydata.getDay()) {
        case 0:
            z = "日";
            break;
        case 1:
            z = "一";
            break;
        case 2:
            z = "二";
            break;
        case 3:
            z = "三";
            break;
        case 4:
            z = "四";
            break;
        case 5:
            z = "五";
            break;
        case 6:
            z = "六";
            break;
        default :
            z = "一";
            break;
    }
    if (String(f).split("").length == 1) {
        f = "0" + String(f);
    }
    if (s < 6) {
        state = "凌晨好"
    }
    else if (s < 9) {
        state = "早上好"
    }
    else if (s < 12) {
        state = "上午好"
    }
    else if (s < 14) {
        state = "中午好"
    }
    else if (s < 17) {
        state = "下午好"
    }
    else if (s < 19) {
        state = "傍晚好"
    }
    else if (s < 22) {
        state = "晚上好"
    }
    else {
        state = "夜里好"
    }

}

zdrysc.getTime();

setInterval(function () {
    zdrysc.getTime()
}, 30000);

//分页插件居中函数
zdrysc.setpagecss = function () {
    var laypage = $('.layui-laypage-default')
    laypage.css({
        'margin-left': ($('.listpage').width() - laypage.width()) / 2
    })
}

//退出登录
$(".logout").click(function () {
    $.ajax({
        url:"/logout",
        type: "get",
        xhrFields: {
            withCredentials: true
        },
        data: {},
        success: function (res) {
            if (res != "logout->success") {
                return layer.msg(res, {anim: 6});
            }
            sessionStorage.removeItem("zdryscuser");
            if($(".title").attr("data-href")=="shou"){
                window.location = "login.html";
            }else{
                window.location = "../login.html";
            }
        }
    });
});

$(".go_person").click(function () {
    if($(".title").attr("data-href")=="shou"){
        window.location = "html/person.html";
    }else{
        window.location = "person.html";
    }
});

//时间戳格式转换换
zdrysc.timechange = function (time) {
    var timedata = new Date(time);
    var y = timedata.getFullYear();
    var m = ((timedata.getMonth() + 1) > 9 ? (timedata.getMonth() + 1) : '0' + (timedata.getMonth() + 1));
    var d =  (timedata.getDate() > 9 ? timedata.getDate() : '0' + timedata.getDate());
    var h = (timedata.getHours() > 9 ? timedata.getHours() : '0' + timedata.getHours());
    var min = (timedata.getMinutes() > 9 ? timedata.getMinutes() : '0' + timedata.getMinutes());
    var s = (timedata.getSeconds() > 9 ? timedata.getSeconds() : '0' + timedata.getSeconds());
    return y+'-'+m+'-'+d+'  '+h+':'+min+':'+s;
};



//个人信息设置
var zdryscuserdata = JSON.parse(sessionStorage.getItem("zdryscuser"));
if(zdryscuserdata){
    console.log(zdryscuserdata);
    $(".usernamecon").html(zdryscuserdata.name);
    if(zdryscuserdata.jobLevel=="normal"){
        $(".com_jifen,.com_gunli").hide();
    }else if(zdryscuserdata.jobLevel=="group"){
        $(".com_gunli").hide();
    }
    if(zdryscuserdata.image){
        $(".headerimg").html('<img src="'+zdryscuserdata.image+'" alt="">');
        $("#demo1").html('<img src="'+zdryscuserdata.image+'" alt="">');
    }
}