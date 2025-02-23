setTimeout(() => {
    document.querySelectorAll(".alert-fixed").forEach(el => {
        el.style.transition = "opacity 0.5s";
        el.style.opacity = "0";
        setTimeout(() => el.remove(), 500);
    });
}, 3000);

//
function validateForm() {
    let input = document.getElementById("storeName");
    let name = input.value.trim();
    let errorIcon = document.getElementById("errorIcon");

    if (name === "") {
        alert("Tên cửa hàng không được để trống!");
        errorIcon.classList.remove("hidden");
        input.classList.add("border-red-500");
        return false;
    }

    errorIcon.classList.add("hidden");
    input.classList.remove("border-red-500");
    return true;
}
// noti.js




