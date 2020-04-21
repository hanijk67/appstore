 $(document).ready(function() {
  $('#fullpage').fullpage({
    css3: true,
    controlArrowColor: "#000",
    menu: '#myMenu',
    navigation: true,
    navigationTooltips: [
      '<a class="hvr-sink" href="#"> <div class="download-custom-text"> دانلود به صورت انتخابی</div> <div class="mdi mdi-chevron-down animated-arrow-1"></div><div class="mdi mdi-chevron-down animated-arrow-2"></div></a>']
  });
});

  // $(document).ready(function() {

  //   console.log($(".section p").css("font-size"));
  // });

$(document).ready(function(){
    $(".toggle").click(function(){
        $(".toggle").toggleClass("fu");
    });
});
