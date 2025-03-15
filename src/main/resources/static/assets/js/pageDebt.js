function filterDebts() {
    let filterFromId = document.getElementById("search-id-min1").value.toLowerCase();
    let filterToId = document.getElementById("search-id-max1").value.toLowerCase();
    let filterNote = document.getElementById("search-note").value.toLowerCase();
    let filterType = document.getElementById("search-type").value.toLowerCase();
    let filterDebtFrom = document.getElementById("search-debt-min1").value.toLowerCase();
    let filterDebtTo = document.getElementById("search-debt-max1").value.toLowerCase();

    let filterFromCreated = document.getElementById("search-date-min1").value;
    let filterToCreated = document.getElementById("search-date-max1").value;


    let rows = document.querySelectorAll("#tableBody tr");

    // Chuyển đổi thành đối tượng Date
    let fromCreatedDate = filterFromCreated ? parseCustomDate(filterFromCreated, false) : null;
    let toCreatedDate = filterToCreated ? parseCustomDate(filterToCreated, true) : null;

    let totalFiltered = 0;

    rows.forEach(row => {
        let id = row.cells[0].innerText.toLowerCase();
        let note = row.cells[1].innerText.toLowerCase();
        let type = row.cells[2].innerText.toLowerCase();
        let amount = row.cells[3].innerText.toLowerCase();
        let createdAt = row.cells[4].innerText;
        let createdAtDate = parseCustomDate(createdAt); // Chuyển đổi ngày từ bảng


        let match2 = true;

        if (filterFromId && parseInt(id) < parseInt(filterFromId)) match2 = false;
        if (filterToId && parseInt(id) > parseInt(filterToId)) match2 = false;
        if (filterNote && !note.includes(filterNote)) match2 = false;
        if (filterType && type.toLowerCase() !== filterType.toLowerCase()) match2 = false;
        if (filterDebtFrom && parseFloat(amount) < parseInt(filterDebtFrom)) match2 = false;
        if (filterDebtTo && parseFloat(amount) > parseInt(filterDebtTo)) match2 = false;
        if (fromCreatedDate && createdAtDate < fromCreatedDate) match2 = false;
        if (toCreatedDate && createdAtDate > toCreatedDate) match2 = false;

        if (match2) {
            row.style.display = '';
            totalFiltered++;
        } else {
            row.style.display = 'none';
        }
    });

    // document.querySelector(".total-stores").textContent = `Tổng cộng: ${totalFiltered} cửa hàng`;
}

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll('input').forEach(input => {
        input.addEventListener('input', filterDebts);
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
        filterDebts();
    });
});
function parseCustomDate(dateStr, isToDate = false) {
    if (!dateStr) return null;
    const parts = dateStr.split(' '); // Tách phần ngày và giờ
    const datePart = parts[0]; // Ví dụ: "25/10/2023"
    let timePart = parts[1] || (isToDate ? '23:59:59' : '00:00:00'); // Mặc định giờ nếu không có
    const [day, month, year] = datePart.split('/'); // Tách ngày, tháng, năm
    const [hours, minutes, seconds = '00'] = timePart.split(':'); // Tách giờ, phút, giây
    return new Date(year, month - 1, day, hours, minutes, seconds); // Tạo đối tượng Date
}