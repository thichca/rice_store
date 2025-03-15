
function filterZones() {
    let filterFromId = document.getElementById("search-zoneid-min").value.toLowerCase();
    let filterToId = document.getElementById("search-zoneid-max").value.toLowerCase();
    let filterName = document.getElementById("search-zonename").value.toLowerCase();
    let filterAddress = document.getElementById("search-zoneaddress").value.toLowerCase();
    let filterFromCreated = document.getElementById("search-date-zonemin").value;
    let filterToCreated = document.getElementById("search-date-zonemax").value;
    let filterFromUpdated = document.getElementById("search-update-zonemin").value;
    let filterToUpdated = document.getElementById("search-update-zonemax").value;

    let rows = document.querySelectorAll("#tableBody tr");

    let fromCreatedDate = filterFromCreated ? parseCustomDate1(filterFromCreated , false): null;
    let toCreatedDate = filterToCreated ? parseCustomDate1(filterToCreated, true) : null;
    let fromUpdatedDate = filterFromUpdated ? parseCustomDate1(filterFromUpdated ,false): null;
    let toUpdatedDate = filterToUpdated ? parseCustomDate1(filterToUpdated, true) : null;

    let totalFiltered = 0;

    rows.forEach(row => {
        let id = row.cells[0].innerText.toLowerCase();
        let name = row.cells[1].innerText.toLowerCase();
        let address = row.cells[2].innerText.toLowerCase();
        let createdAt1 = row.cells[3].innerText;
        let updatedAt1 = row.cells[4].innerText;
        let createdAtDate1 =  parseCustomDate1(createdAt1);
        let updatedAtDate1 =  parseCustomDate1(updatedAt1);

        let match1 = true;

        if (filterFromId && parseInt(id) < parseInt(filterFromId)) match1 = false;
        if (filterToId && parseInt(id) > parseInt(filterToId)) match1 = false;
        if (filterName && !name.includes(filterName)) match1 = false;
        if (filterAddress && !address.includes(filterAddress)) match1 = false;
        if (fromCreatedDate && createdAtDate1 < fromCreatedDate) match1 = false;
        if (toCreatedDate && createdAtDate1 > toCreatedDate) match1 = false;
        if (fromUpdatedDate && updatedAtDate1 < fromUpdatedDate) match1 = false;
        if (toUpdatedDate && updatedAtDate1 > toUpdatedDate) match1 = false;

        if (match1) {
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
        input.addEventListener('input', filterZones);
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

        filterZones();
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