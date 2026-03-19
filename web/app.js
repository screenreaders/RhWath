(() => {
  const status = document.getElementById("instructionStatus");
  const target = document.getElementById("instructionText");

  if (!status || !target) {
    return;
  }

  fetch("instructions.txt", { cache: "no-store" })
    .then((resp) => {
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}`);
      }
      return resp.text();
    })
    .then((text) => {
      target.textContent = text.trim();
      status.textContent = "Instrukcje załadowane.";
    })
    .catch(() => {
      status.textContent =
        "Nie można załadować instrukcji. Pobierz plik tekstowy.";
      target.textContent = "";
    });

  const countTargets = Array.from(
    document.querySelectorAll(".card__count[data-file]")
  );
  if (countTargets.length) {
    fetch("counts.php", { cache: "no-store" })
      .then((resp) => {
        if (!resp.ok) {
          throw new Error(`HTTP ${resp.status}`);
        }
        return resp.json();
      })
      .then((data) => {
        countTargets.forEach((el) => {
          const file = el.getAttribute("data-file");
          const value =
            file && Object.prototype.hasOwnProperty.call(data, file)
              ? data[file]
              : 0;
          el.textContent = `Pobrań: ${value}`;
        });
      })
      .catch(() => {
        countTargets.forEach((el) => {
          el.textContent = "Pobrań: brak danych";
        });
      });
  }
})();
