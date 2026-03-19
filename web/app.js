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
      status.textContent = "Instrukcje zaladowane.";
    })
    .catch(() => {
      status.textContent =
        "Nie mozna zaladowac instrukcji. Pobierz plik tekstowy.";
      target.textContent = "";
    });
})();
