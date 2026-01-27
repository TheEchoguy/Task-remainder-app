<script>
    let cropper;
    const avatar = document.getElementById("avatarPreview");
    const cropSection = document.getElementById("cropSection");
    const cropImage = document.getElementById("cropImage");
    const input = document.getElementById("imageInput");
    const applyBtn = document.getElementById("applyCrop");

    applyBtn.style.display = "none";

    input.addEventListener("change", function (e) {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = () => {

            // Hide avatar
            avatar.parentElement.classList.add("hidden");

            // Show crop section
            cropSection.classList.remove("hidden");
            applyBtn.style.display = "inline-block";

            cropImage.src = reader.result;

            if (cropper) cropper.destroy();
            cropper = new Cropper(cropImage, {
                aspectRatio: 1,
                viewMode: 1,
                autoCropArea: 1,
                background: false
            });
        };
        reader.readAsDataURL(file);
    });

    applyBtn.addEventListener("click", () => {
        if (!cropper) return;

        const canvas = cropper.getCroppedCanvas({
            width: 300,
            height: 300
        });

        canvas.toBlob(blob => {
            const file = new File([blob], "profile.png", { type: "image/png" });

            const dt = new DataTransfer();
            dt.items.add(file);
            input.files = dt.files;

            // Restore avatar
            avatar.src = canvas.toDataURL();
            avatar.parentElement.classList.remove("hidden");

            // Cleanup
            cropSection.classList.add("hidden");
            applyBtn.style.display = "none";
            cropper.destroy();
            cropper = null;
        });
    });
</script>
