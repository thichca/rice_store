
function filterInvoices() {
    let filterFromId = document.getElementById("id1").value.toLowerCase();
    let filterToId = document.getElementById("id2").value.toLowerCase();
    let filterName = document.getElementById("name").value.toLowerCase();
    let filterPay = document.getElementById("quantity").value.toLowerCase();
    let filterTotalFrom = document.getElementById("totalPrice").value.toLowerCase();
    let filterTotalTo = document.getElementById("totalPrice1").value.toLowerCase();
    let filterFromCreated = document.getElementById("create1").value;
    let filterToCreated = document.getElementById("create2").value;
    let filterFromUpdated = document.getElementById("update1").value;
    let filterToUpdated = document.getElementById("update2").value;

    let rows = document.querySelectorAll("#tableBody tr");

    let fromCreatedDate = filterFromCreated ? parseCustomDate1(filterFromCreated , false): null;
    let toCreatedDate = filterToCreated ? parseCustomDate1(filterToCreated, true) : null;
    let fromUpdatedDate = filterFromUpdated ? parseCustomDate1(filterFromUpdated ,false): null;
    let toUpdatedDate = filterToUpdated ? parseCustomDate1(filterToUpdated, true) : null;

    let totalFiltered = 0;

    rows.forEach(row => {
        let id = row.cells[0].innerText.toLowerCase();
        let name = row.cells[1].innerText.toLowerCase();
        let quantity = row.cells[2].innerText.toLowerCase();
        let totalPrice = row.cells[3].innerText.toLowerCase();
        let createdAt2 = row.cells[4].innerText;
        let updatedAt2 = row.cells[5].innerText;
        let createdAtDate2 =  parseCustomDate1(createdAt2);
        let updatedAtDate2 =  parseCustomDate1(updatedAt2);

        let match3 = true;

        if (filterFromId && parseInt(id) < parseInt(filterFromId)) match3 = false;
        if (filterToId && parseInt(id) > parseInt(filterToId)) match3 = false;
        if (filterName && !name.includes(filterName)) match3 = false;
        if (filterPay && quantity.toLowerCase() !== filterPay.toLowerCase()) match3 = false;
        if (filterTotalFrom && parseFloat(totalPrice) < parseInt(filterTotalFrom)) match3 = false;
        if (filterTotalTo && parseFloat(totalPrice) > parseInt(filterTotalTo)) match3 = false;
        if (fromCreatedDate && createdAtDate2 < fromCreatedDate) match3 = false;
        if (toCreatedDate && createdAtDate2 > toCreatedDate) match3 = false;
        if (fromUpdatedDate && updatedAtDate2 < fromUpdatedDate) match3 = false;
        if (toUpdatedDate && updatedAtDate2 > toUpdatedDate) match3 = false;

        if (match3) {
            row.style.display = '';
            totalFiltered++;
        } else {
            row.style.display = 'none';
        }
    });

    //document.querySelector(".total-stores").textContent = `Tổng cộng: ${totalFiltered} cửa hàng`;
}

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll('input').forEach(input => {
        input.addEventListener('input', filterInvoices());
    });

    const clearButton = document.getElementById("clearFilters");
    clearButton.addEventListener("click", function () {
        document.querySelectorAll("tr.bg-gray-50 input").forEach(input => {
            input.value = "";
        });

        let rows = document.querySelectorAll("#tableBody tr");
        rows.forEach(row => {
            row.style.display = '';
        });

        filterInvoices();
    });

});
function parseCustomDate1(dateStr1, isToDate1 = false) {
    if (!dateStr1) return null;
    const parts = dateStr1.split(' '); // Tách phần ngày và giờ
    const datePart = parts[0]; // Ví dụ: "25/10/2023"
    let timePart = parts[1] || (isToDate1 ? '23:59:59' : '00:00:00'); // Mặc định giờ nếu không có
    const [day, month, year] = datePart.split('/'); // Tách ngày, tháng, năm
    const [hours, minutes, seconds = '00'] = timePart.split(':'); // Tách giờ, phút, giây
    return new Date(year, month - 1, day, hours, minutes, seconds); // Tạo đối tượng Date
}