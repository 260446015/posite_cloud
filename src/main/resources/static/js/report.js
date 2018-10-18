

//分页设置
layui.use(['laypage','layer','element','form']);
var laypage = layui.laypage;
var element = layui.element;
var form = layui.form;
var oajax = [];

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
                        oclass = "yjfont_red";
                        aclass = "yj_red";
                        break
                    case "橙色预警":
                        oclass = "yjfont_orange";
                        aclass = "yj_orange";
                        break
                    case "蓝色预警":
                        oclass = "yjfont_biue";
                        aclass = "yj_blue";
                        break
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
                        '<span><input id="'+item.id+'" type="checkbox" name="deleteli" lay-skin="primary" lay-filter="xuanzhong"></span>' +
                        '<span>'+number+'</span>' +
                        '<span>'+item.sorce+'</span>' +
                        '<span class="sc_zdspan" data-href="'+appspsn+'">'+odata+'</span>' +
                        '<span>'+zdrysc.timechange(item.creationTime)+'</span>' +
                        '<span><div class="layui-btn layui-btn-normal baocunbtn" data-href="'+item.mobile+'">查看报告</div></span>' +
                        '</div>';
                }else{
                    list = '<div>' +
                        '<span><input id="'+item.id+'" type="checkbox" name="deleteli" lay-skin="primary" lay-filter="xuanzhong"></span>' +
                        '<span>'+number+'</span>' +
                        '<span>'+item.sorce+'</span>' +
                        '<span class="sc_zdspan" data-href="'+appspsn+'">'+odata+'</span>' +
                        '<span>'+zdrysc.timechange(item.creationTime)+'</span>' +
                        '<span><div class="layui-btn layui-btn-normal baocunbtn" data-href="'+item.mobile+'">查看报告</div></span>' +
                        '</div>';
                }
                $(".zd_boxlist").append(list);
                form.render();
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

$(".baocunbtn1").click(function () {
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


//是否全选
form.on('checkbox(quanxuan)', function(data){
    if(data.elem.checked){
        $(".baocunbtn12").attr("data-val",false);
        $("input[name='deleteli']").prop('checked',true);
    }else{
        $("input[name='deleteli']").prop('checked',false);
        $(".baocunbtn12").attr("data-val",true);
    }
    form.render();
});

//选中
form.on('checkbox(xuanzhong)', function(data){
    var oid = data.elem.getAttribute("id");
    var key = $.inArray(oid,oajax);
    if(data.elem.checked){
        if(key=="-1"){
            oajax.push(oid);
        }
    }else{
        oajax.splice($.inArray(oid,oajax),1);
    }
});


//点击生成局部报告
$(".baocunbtn12").click(function () {
    if($(this).attr("data-val")=="false"){
        oajax = ['123','456'];
        sessionStorage.setItem("zdrsc_data",JSON.stringify(oajax));
        window.location = "reportall.html?true";
    }else{
        if(oajax.length==0){
            return layer.msg("请标记要生成报告的号码；");
        }
        sessionStorage.setItem("zdrsc_data",JSON.stringify(oajax));
        window.location = "reportall.html?false"
    }
});