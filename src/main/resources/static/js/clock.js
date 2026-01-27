function startClock() {
    const clock = document.getElementById("liveClock");
    if (!clock) return;

    function updateClock() {
        const now = new Date();

        let hours = now.getHours();
        let minutes = now.getMinutes();
        let period = hours >= 12 ? "PM" : "AM";

        hours = hours % 12;
        hours = hours ? hours : 12;
        minutes = minutes < 10 ? "0" + minutes : minutes;

        clock.textContent = hours + ":" + minutes + " " + period;
    }

    updateClock();
    setInterval(updateClock, 1000);
}

document.addEventListener("DOMContentLoaded", startClock);
