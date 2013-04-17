$(function() {
	var dlg = $("#dialog").dialog({
		autoOpen : false,
		draggable : false,
		resizable : false,
		width : 500
	});

	$(".physical_machine").hover(function() {
		dlg.dialog("open");
	}, function() {
		dlg.dialog('close');
	}).mousemove(function(event) {
		dlg.dialog("option", "position", {
			my : "left+10 top",
			at : "right+10 bottom",
			of : event,
			collision : 'none'
		});
	});
});
