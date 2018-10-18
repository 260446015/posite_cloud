

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
                var obdata = JSON.parse($(".listpage").attr("data-href"));
                getimportlist(obdata.maxSorce,obdata.minSorce,obdata.mobile,(obj.curr - 1),obj.limit,obdata.webname,obdata.webtype);
            }
        }
    });
}


function getimportlist(maxSorce,minSorce,mobile,pageNum,pageSize,webname,webtype) {
    $(".zd_boxlist").empty();
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
            webname: webname,
            webtype: webtype
        }),
        contentType: "application/json",
        success: function (res) {
            console.log(res);
            if (res.code != 0) {
                //return layer.msg(res.message, {anim: 6});
            }
            gobacklogout(res.code);
            if (0 === pageNum) {
                count = res.data.totalNumber;
                setPageNo(count,pageSize);
            }
            if(!res.data.dataList){
                $(".zd_boxlist").append('<h3 class="kongbai"><img src="../img/zanwushuju.png" alt=""></h3>');
            }
            $.each(res.data.dataList,function (i,item) {
                var appname = '';
                var appspsn = '';
                var odata;
                var ohandleMark;
                var oclass = "";
                var aclass = "";
                if(item.handleMark){
                    if(item.handleMark>=0){
                        ohandleMark = item.handleMark;
                    }else{
                        ohandleMark = 0;
                    }
                }else{
                    ohandleMark = 0;
                }
                $.each(item.data,function (i,item) {
                    if(i==0){
                        appname+=item.webname
                    }else{
                        appname+="，"+item.webname
                    }
                    appspsn+="<span class='sc_zdgrayspan'>"+item.webtype+"："+item.webname+"</span>";
                });
                switch (item.warnInfo){
                    case "红色预警":
                        oclass = "border-color:#ff000078;";
                        break
                    case "橙色预警":
                        oclass = "border-color:#ff9416;";
                        break
                    default:
                        oclass = "";
                }
                if(item.data==null){
                    odata = "采集中..."
                }else{
                    odata = appname;
                }
                var number;
                if((/^1[34578]\d{9}$/.test(item.mobile))){
                    number = item.mobile;
                }else{
                    number = decrypt(item.mobile);
                }
                if(i%2){
                    list = '<div class="sc_zdgray">' +
                        '<span>'+number+'</span>' +
                        '<span>'+item.sorce+'</span>' +
                        '<span class="sc_zdspan" data-href="'+appspsn+'">'+odata+'</span>' +
                        '<span>'+zdrysc.timechange(item.creationTime)+'</span>' +
                        '<span><select id="'+item.id+'" class="sellayui" style="'+oclass+aclass+'">' +
                        '<option value="0">未处理</option>' +
                        '<option value="3">已通报</option>' +
                        '<option value="1">处理中</option>' +
                        '<option value="2">已处理</option>' +
                        '</select></span>' +
                        '</div>';
                }else{
                    list = '<div>' +
                        '<span>'+number+'</span>' +
                        '<span>'+item.sorce+'</span>' +
                        '<span class="sc_zdspan" data-href="'+appspsn+'">'+odata+'</span>' +
                        '<span>'+zdrysc.timechange(item.creationTime)+'</span>' +
                        '<span><select id="'+item.id+'" class="sellayui" style="'+oclass+aclass+'">' +
                        '<option value="0">未处理</option>' +
                        '<option value="3">已通报</option>' +
                        '<option value="1">处理中</option>' +
                        '<option value="2">已处理</option>' +
                        '</select></span>' +
                        '</div>';
                }
                $(".zd_boxlist").append(list).find(".sellayui").eq(i).val(ohandleMark)
            });
        }
    });
}

//点击详情
$(".zd_boxlist").on("click",".sc_zdspan",function () {
    if($(this).attr("data-href")){
        layer.open({
            type: 1,
            shade: false,
            title: false, //不显示标题
            content: $(this).attr("data-href")
        });
    }
});

//生成报告
$(".zd_boxlist").on("click",".baocunbtn",function () {
    window.location = "report.html?"+$(this).attr('data-href');
});

//点击查询
form.on('select(aihao)', function(data){
    $(".ba_fenlei").attr("data-href",data.value);
});

$(".baocunbtn").click(function () {
    var data = JSON.stringify({
        maxSorce: $(".ba_maxcore").val(),
        minSorce: $(".ba_minscore").val(),
        mobile: $(".ba_number").val(),
        webname: $(".ba_pingtai").val(),
        webtype: $(".ba_fenlei").attr("data-href")
    });
    $(".listpage").attr("data-href",data);
    getimportlist($(".ba_maxcore").val(),$(".ba_minscore").val(),$(".ba_number").val(),0,10,$(".ba_pingtai").val(),$(".ba_fenlei").attr("data-href"));
});

//修改状态
$(".zd_boxlist").on("change",".sellayui",function () {
    //console.log($(this).val(),$(this).attr("id"))
    var data = {
        "handleMark": $(this).val(),
        "id": $(this).attr("id")
    };
    $.ajax({
        url: "/api/updatePersonMark",
        data: data,
        type: "patch",
        success: function (res) {
            //console.log(res.data);
            layer.msg("状态修改成功");
        }
    })
})