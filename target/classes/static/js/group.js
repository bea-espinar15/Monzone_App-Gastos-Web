const expensesTable = document.getElementById("expensesTable");
const debtsTable = document.getElementById("debtsTable");
const balancesTable = document.getElementById("balancesTable");
const groupId = expensesTable.dataset.groupid;
const currencyString = expensesTable.dataset.currency;
const userId = expensesTable.dataset.userid;


// Render EXISTING Expenses
go(`${config.rootUrl}/group/${groupId}/getExpenses`, "GET")
    .then(expenses => {
        expensesTable.insertAdjacentHTML("afterbegin", `<h2 id="exp-none" style="display: none; text-align: center; font-size: 16px; margin-top: 20px; text-transform: uppercase; letter-spacing: 2px;">You don't have expenses yet</h2>`);
        const e = document.getElementById('exp-none');
        if(expenses.length == 0)
            e.style.display = 'block';
        else{
            expenses.forEach(expense => {
                expensesTable.insertAdjacentHTML("afterbegin", renderExpense(expense));
            })
        }       
    }
    );

// Render INCOMING Expenses
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        // If expense destined to current group
        if (obj.type == "EXPENSE" && destination.includes(groupId)) {
            const expense = obj.expense;
            const expenseHTML = document.getElementById(`expense-${expense.expenseId}`)
            switch (obj.action) {
                case "EXPENSE_CREATED":
                    expensesTable.insertAdjacentHTML("afterbegin", renderExpense(expense));
                    renderAllBalances()
                    break;
                case "EXPENSE_MODIFIED":
                    expenseHTML.outerHTML = renderExpense(expense);
                    renderAllBalances()
                    break;
                case "EXPENSE_DELETED":
                    expenseHTML.parentElement.removeChild(expenseHTML);
                    renderAllBalances()
                    break;
                default:
            }
            // Render mensaje no existen grupos
            const e = document.getElementById('exp-none');
            if (e.style.display === 'none' && expensesTable.childElementCount == 1) // El 1 es el mensaje de vacio
                e.style.display = 'block';  // Mostrar el elemento
            else if(expensesTable.childElementCount > 1)
                e.style.display = 'none';  // Ocultar el elemento    

            renderAllDebts();
        }
        else if (obj.type == "GROUP" && obj.action == "GROUP_MEMBER_REMOVED") {
            const member = obj.group.members.find(member => member.idUser == userId);
            if (member != null && !member.enabled) {
                console.log("Redirecting to ", "/user/");
                window.location.replace("/user/");
            }
        }
    }
}

// Render single expense
function renderExpense(expense) {
    const truncatedAmount = Number(expense.amount).toFixed(2);
    return `<div id="expense-${expense.expenseId}" class="col">
                <div class="card justify-content-center border-light text-white mb-3 mx-auto" role="button" onclick="location.href='/group/${groupId}/${expense.expenseId}'">
                    <div class="row row-cols-2 row-cols-md-4 g-0">
                        <!-- Icon -->
                        <div class="icon-card me-3 col-4 col-md-2 d-flex align-items-center justify-content-center">
                            <img src="/img/type/${expense.typeID}.png" alt="Category Icon" class="icon">
                        </div>
                        <!-- Text Info -->
                        <div class="col-7 col-md-5">
                            <div class="row card-text-row">
                                <div class="card-title">${expense.name}</div>
                            </div>
                            <div class="row card-text-row">
                                <div class="card-subtitle">Paid by ${expense.paidByID == userId ? "you" : expense.paidByName}</div>
                            </div>
                        </div>
                        <!-- Date -->
                        <div class="col-md-2 d-flex align-items-center justify-content-center text-nowrap">
                            <div class="card-text">${expense.date}</div>
                        </div>
                        <!-- Amount -->
                        <div class="col-md-2 d-flex align-items-center justify-content-center expense-amount">
                            <div class="card-text">${truncatedAmount} ${currencyString}</div>
                        </div>
                    </div>
                </div>
            </div>`
}

// Llamada inicial
renderAllDebts()
renderAllBalances()

function renderAllDebts() {
    debtsTable.innerHTML = '';  // clear debts table

    // Get and render debts
    go(`${config.rootUrl}/group/${groupId}/getDebts`, "GET")
        .then(debts => {
            debts.forEach(debt => {
                debtsTable.insertAdjacentHTML("afterbegin", renderDebt(debt));
            })

            if (debts.length == 0) {
                debtsTable.insertAdjacentHTML("afterbegin", renderNoDebts());
            } else {
                document.querySelectorAll(".btn-settle").forEach((btn) => {
                    btn.onclick = (e) => handleSettleDebtClick(btn, e);
                });renderDebt
            }
        }
        );
}

// Render single debt
function renderDebt(debt) {
    const truncatedAmount = Number(debt.amount).toFixed(2);
    return `<div class="row">
                <div class="col-4">
                    <form method="post" action="/group/${groupId}/settle">
                        <input type="hidden" name="_csrf" value="${config.csrf.value}">
                        <input type="hidden" name="debtorId" value="${debt.idDebtor}">
                        <input type="hidden" name="debtOwnerId" value="${debt.idDebtOwner}">
                        <input type="hidden" name="amount" value="${debt.amount}">
                        <button type="button" class="btn-settle markPaid mb-2">Mark as paid</button>
                    </form>
                </div>
                <div class="col-8">
                    <label>${debt.idDebtor == userId ? "You owe" : debt.debtorName + " owes"} ${debt.idDebtOwner == userId ? "you" : debt.debtOwnerName} - ${truncatedAmount} ${currencyString}</label>
                </div>
            </div>`
}

// Render no debts message
function renderNoDebts() {
    return `<div class="row">
                <div class="col">
                    You don't have debts to settle yet
                </div>
            </div>`
}

// Render member balances
function renderAllBalances() {
    balancesTable.innerHTML = '';  // clear debts table

    go(`${config.rootUrl}/group/${groupId}/getMembers`, "GET")
        .then(members => {
            members.forEach(member => {
                balancesTable.insertAdjacentHTML("beforeend", renderMemberBalance(member));
            })
        }
        );
}

function renderMemberBalance(member) {
    const truncatedAmount = Number(member.balance).toFixed(2);
    if (truncatedAmount < 0) {
        return `<div class="row">
                    <div class="col text-center">
                        <h5 class="back_balance_neg">${truncatedAmount}${currencyString}</h5>
                    </div>   
                    <div class="col text-center">
                        <h5>${member.idUser == userId ? "You" : member.username}</h5>
                    </div>                             
                </div>`;
    }
    else if (truncatedAmount > 0) {
        return `<div class="row">
                    <div class="col text-center">
                        <h5>${member.idUser == userId ? "You" : member.username}</h5>
                    </div>
                    <div class="col text-center">
                        <h5 class="back_balance_pos">${truncatedAmount}${currencyString}</h5>
                    </div>                                
                </div>`;
    }
    else {
        return `<div class="row">
                    <div class="col text-center">
                        <h5>${member.idUser == userId ? "You" : member.username}</h5>
                    </div>
                    <div class="col text-center">
                        <h5>0.00 ${currencyString}</h5>
                    </div>                                
                </div>`;
    }
}

// Mark debt as settled
function handleSettleDebtClick(btn, e) {
    e.preventDefault();
    console.log("Settling debt");

    const debtorId = btn.parentNode.querySelector('input[name="debtorId"]').value;
    const debtOwnerId = btn.parentNode.querySelector('input[name="debtOwnerId"]').value;
    const amount = btn.parentNode.querySelector('input[name="amount"]').value;

    go(btn.parentNode.action, "POST", {
        debtorId,
        debtOwnerId,
        amount,
    })
        .then((d) => {
            console.log("Settle: success", d);
            createToastNotification(`debt-settled-${debtorId}-${debtOwnerId}`, "Debt has successfully been settled.");
        })
        .catch(e => {
            console.log("Error settling debt", e);
            createToastNotification(`error-debt-settling`, JSON.parse(e.text).message, true);
        })
}
