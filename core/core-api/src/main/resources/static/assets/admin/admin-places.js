(() => {
    const table = document.querySelector("[data-admin-places-table]");

    if (!table) {
        return;
    }

    table.addEventListener("submit", async (event) => {
        const form = event.target.closest("[data-place-action-form]");

        if (!form) {
            return;
        }

        event.preventDefault();
        await submitPlaceAction(form);
    });

    async function submitPlaceAction(form) {
        const button = form.querySelector("button");

        if (button) {
            button.disabled = true;
        }

        try {
            const response = await fetch(form.action, {
                method: "POST",
                body: new FormData(form),
                headers: {
                    "Accept": "application/json",
                    "X-Requested-With": "XMLHttpRequest"
                },
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error(`Admin place action failed: ${response.status}`);
            }

            applyPlaceAction(await response.json(), form);
        } catch (error) {
            form.removeAttribute("data-place-action-form");
            form.submit();
        } finally {
            if (button) {
                button.disabled = false;
            }
        }
    }

    function applyPlaceAction(result, form) {
        const row = table.querySelector(`[data-place-row="${result.id}"]`);

        updateNotice(result.message);
        updatePageSummary(result);

        if (!row) {
            return;
        }

        if (!result.visible) {
            row.remove();
            ensureEmptyRow();
            return;
        }

        row.dataset.placeStatus = result.status;
        row.querySelector("[data-place-status-label]").textContent = result.status;
        replaceActionForm(row, form, result);
    }

    function updateNotice(message) {
        const notice = document.querySelector("[data-admin-notice]");

        if (!notice) {
            return;
        }

        notice.textContent = message;
        notice.classList.remove("is-hidden");
    }

    function updatePageSummary(result) {
        const count = document.querySelector("[data-place-count]");
        const pageIndicator = document.querySelector("[data-place-page-indicator]");

        if (count) {
            count.textContent = `총 ${result.totalCount}개 · 페이지당 ${result.pageSize}개`;
        }

        if (pageIndicator) {
            pageIndicator.textContent = `${result.currentPage} / ${result.totalPages}`;
        }
    }

    function replaceActionForm(row, currentForm, result) {
        const actions = row.querySelector("[data-place-actions]");

        if (!actions) {
            return;
        }

        actions.replaceChildren(buildActionForm(currentForm, result));
    }

    function buildActionForm(currentForm, result) {
        const form = document.createElement("form");

        form.method = "post";
        form.action = result.nextActionUrl;
        form.dataset.placeActionForm = "";
        appendCopiedHiddenInput(form, currentForm, "_csrf");
        appendCopiedHiddenInput(form, currentForm, "includeHidden");
        appendCopiedHiddenInput(form, currentForm, "page");

        const button = document.createElement("button");
        button.type = "submit";
        button.textContent = result.nextActionLabel;
        button.className = result.nextActionStyle === "danger"
                ? "admin-danger-button"
                : "admin-secondary-button";
        form.appendChild(button);

        return form;
    }

    function appendCopiedHiddenInput(form, currentForm, name) {
        const source = currentForm.querySelector(`input[name="${name}"]`);

        if (!source) {
            return;
        }

        const input = document.createElement("input");
        input.type = "hidden";
        input.name = source.name;
        input.value = source.value;
        form.appendChild(input);
    }

    function ensureEmptyRow() {
        const body = table.querySelector("tbody");

        if (!body || body.querySelector("[data-place-row]")) {
            return;
        }

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        row.dataset.placeEmptyRow = "";
        cell.colSpan = 5;
        cell.textContent = "등록된 장소가 없습니다.";
        row.appendChild(cell);
        body.appendChild(row);
    }
})();
