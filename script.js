
//var printData = function(input) {
//	console.log()
//	const $btnPrint = document.querySelector("#btnPrint");
//	$btnPrint.addEventListener("click", () => {
//	    window.print();
//	});
//
//}



function printPreview() {
 var Contractor= 'Sai Ram Contractor';
 printWindow = window.open("", "", "location=1,status=1,scrollbars=1,width=650,height=600");
 printWindow.document.write('<html><head>');
 printWindow.document.write('<style type="text/css">@media print{.no-print, .no-print *{display: none !important;}</style>');
 printWindow.document.write('</head><body>');
 printWindow.document.write('<div style="width:100%;text-align:right">');

  //Print and cancel button
 printWindow.document.write('<input type="button" id="btnPrint" value="Print" class="no-print" style="width:100px" onclick="window.print()" />');
 printWindow.document.write('<input type="button" id="btnCancel" value="Cancel" class="no-print"  style="width:100px" onclick="window.close()" />');

 printWindow.document.write('</div>');

 //You can include any data this way.
 printWindow.document.write('<table><tr><td>Contractor name:'+ Contractor +'</td></tr>This Is test dAta 1 </table');

 printWindow.document.write('This Is tes Data 2');
 //here 'forPrintPreview' is the id of the 'div' in current page(aspx).
 printWindow.document.write('</body></html>');
 printWindow.document.close();
 printWindow.focus();
}