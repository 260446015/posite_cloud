var h = $(window).height();
var h1 = $("body").height();
var w = $(window).width();
console.log(h + "+" + w);
var liwidth = ($(window).width()-$(window).width()*0.06)*0.122/ 0.8;
var ctxer = "10.52.219.8";
// var ctx = "http://10.52.220.13:8080/api/";
var ctx = "http://10.52.219.8:8090/api/";
var clga = {
    base: '中科金联',
    user: 'jianghaifei',
    qq: '1465089870'
};
//时间
var mydata, n, y, r, s, f, m, z, state;
clga.getTime = function () {
    mydata = new Date();
    n = mydata.getFullYear();
    y = ((mydata.getMonth() + 1) > 9 ? (mydata.getMonth() + 1) : '0' + (mydata.getMonth() + 1));
    r =  (mydata.getDate() > 9 ? mydata.getDate() : '0' + mydata.getDate());
    s = (mydata.getHours() > 9 ? mydata.getHours() : '0' + mydata.getHours());
    f = (mydata.getMinutes() > 9 ? mydata.getMinutes() : '0' + mydata.getMinutes());
    m = (mydata.getSeconds() > 9 ? mydata.getSeconds() : '0' + mydata.getSeconds());
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
    $(".times_f").html(s + ":" + f);
    $(".timey_r").html(y + "月" + r + "日");
    $(".time_z").html("星期" + z);
    $(".time_statue").html(state);
};

clga.getTime();

setInterval(function () {
    clga.getTime()
}, 30000);

//分页插件居中函数
clga.setpagecss = function () {
    var laypage = $(".listpagejuzhong").find('.layui-laypage-default')
    laypage.css({
        'margin-left': ($('.listpagejuzhong').width() - laypage.width()) / 2
    })
};

$(".back_index").click(function () {
    if ($(".title").attr("data-href") != "index") {
        window.location = "../../index.html";
    }
});

//顶部导航逻辑
$(".sy_div").mouseenter(function () {
    $(".sy_divul").show();
}).mouseleave(function () {
    $(".sy_divul").hide();
});
//退出登录操作
$(".loginout").click(function () {
    $.ajax({
        url: ctx + "/user/logout",
        type: "get",
        data: {},
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            console.log(res);
            if (res == "logout->success") {
                sessionStorage.removeItem("clgaznum");
                sessionStorage.removeItem("clgauser");
                localStorage.removeItem("clgatoken");
                sessionStorage.removeItem("clgapingtai");
                if ($(".title").attr("data-href") == "index") {
                    window.location = "http://10.52.219.8:8090/front/login.html";
                } else {
                    window.location = "http://10.52.219.8:8090/front/login.html";
                }
            } else {
                return layer.msg(res, {anim: 6});
            }
        }
    });
});

//用户名取出显示
var clmember = sessionStorage.getItem("clgauser");
if (clmember) {
    var cldata = JSON.parse(clmember);
    console.log(cldata)
    $(".cluseaname").html(cldata.name);
    if(cldata.photo){
        if(cldata.photo!="string"){
            $(".defalurimg").attr("src",cldata.photo)
        }
    }
}
//在线人数
clga.zaixianrenshu = function () {
    $.ajax({
        url: ctx + "user/visits",
        type: "get",
        xhrFields: {
            withCredentials: true
        },
        data: {},
        success: function (res) {
            console.log(res);
            if(res.code == -8){
                window.location = "http://10.52.219.8:8090/front/login.html";
            }
            if(res.code==0){
                sessionStorage.setItem("clgaznum", JSON.stringify(res.data));
                clga.shedingzaixian(JSON.stringify(res.data));
            }
        }
    });
};
clga.zaixianrenshu();
//设定访问量与条数
clga.shedingzaixian = function (number) {
    var numdata = JSON.parse(number);
    $(".allfangwen").html(numdata.allCount);
    $(".allzaixian").html(numdata.activeCount);
};
clga.whenoerronheader = function(a) {
    a.onerror = null;
    a.src="../../img/defaultimg.jpg";
};

clga.whenoerronheaderindex = function(a) {
    a.onerror = null;
    a.src="img/defaultimg.jpg";
};

clga.whenoerron = function(a) {
    a.onerror = null;
    a.src="../../img/default.jpg";
};

clga.whenoerronimg = function(a) {
    a.onerror = null;
    a.src="../../img/linksuper1.png";
};

//数组取差集
//数组取差集
clga.arrayIntersect = function (a, b) {
    return $.merge($.grep(a, function (i) {
            return $.inArray(i, b) == -1;
        }), $.grep(b, function (i) {
            return $.inArray(i, a) == -1;
        })
    );
};


//回到顶部
$(document).scroll(function () {
    var scroll = $(document).scrollTop();
    if(scroll>=1000){
        if($("slide").attr("data-href")=="true"){
            $("slide").fadeIn().attr("data-href",false);
        }
    }
});
$(".gotop").click(function () {
    $("html body").animate(({scrollTop:0}),300);
    setTimeout(function () {
        $("slide").hide().attr("data-href",true);
    },500);
});

//浏览器默认变蓝
//document.onselectstart=new Function("return false");

//字符创长度计算
clga.getlength = function (str) {
    if(str==null) return 0;
    if(typeof str != "string"){
        str +="";
    }
    return str.replace(/[^\x00-\xff]/g,"01").length;
};

//时间戳格式转换换
clga.timechange = function (time) {
    var timedata = new Date(time);
    var y = timedata.getFullYear();
    var m = (timedata.getMonth()+1<10?'0'+(timedata.getMonth()+1):timedata.getMonth()+1)+'-';
    var d = timedata.getDate();
    var h = timedata.getHours();
    var min = timedata.getMinutes();
    var s = timedata.getSeconds();
    return y+'-'+m+d+'  '+h+':'+min+':'+s;
};

//数据定位
Array.prototype.indexOf = function (searchElement) {
    for(var i=0;i< this.length;i++){
        if(this[i]==searchElement) return i;
    }
    return -1;
};
//数据删除
Array.prototype.remove = function (val) {
    var index = this.indexOf(val);
    if(index > -1){
        this.splice(index,1);
    }
};
// clga.getToken = function () {
//     return localStorage.getItem("clgatoken");
// };
//
// // // 统一添加token
// $(document).ajaxSend(
//     function (event, request, settings) {
//         if (settings.xhrFields) {
//             settings.xhrFields.withCredentials = true;
//         } else {
//             settings.xhrFields = {withCredentials: true};
//         }
//         settings.crossDomain = true;
//         //统一增加token
//         if (settings.headers) {
//             settings.headers.token = clga.getToken();
//         } else {
//             settings.headers = {"token": clga.getToken()};
//         }
//         request.setRequestHeader("token", clga.getToken());
// });