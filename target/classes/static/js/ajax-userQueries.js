// Cálculo de los GASTOS del MES
// Al cambiar la divisa
document.getElementById("currMonth").addEventListener('change', function () {
    const totalTextMonth = document.getElementById('total-exp');
    const dateString = document.getElementById("date").value;
    const currId = parseInt(document.getElementById("currMonth").value);

    // Cambiar el tipo de moneda según la seleccionada
    let currencyString = "";
    currencyString = getCurrencyString(currId, currencyString);

    go(`${config.rootUrl}/user/getMonthly/${dateString}/${currId}`, "GET", {
    })
        .then(data => {
            totalTextMonth.value = data + currencyString;
        })
});

// Cálculo de los GASTOS del MES
// Al cambiar la fecha
document.getElementById("date").addEventListener('change', function () {
    const totalTextMonth = document.getElementById('total-exp');
    const dateString = document.getElementById("date").value;
    const currId = parseInt(document.getElementById("currMonth").value);

    // Cambiar el tipo de moneda según la seleccionada
    let currencyString = "";
    currencyString = getCurrencyString(currId, currencyString);

    go(`${config.rootUrl}/user/getMonthly/${dateString}/${currId}`, "GET", {
    })
        .then(data => {
            totalTextMonth.value = data + currencyString;
        })
});

// Cálculo de los GASTOS por CATEGORÍAS
// envio de mensajes con AJAX
document.getElementById("currType").addEventListener('change', function () {
    const selector = document.getElementsByClassName('curr');
    const amounts = document.getElementsByClassName('amount');
    const currId = parseInt(document.getElementById("currType").value);

    // Cambiar el tipo de moneda según la seleccionada
    let currencyString = "";
    currencyString = getCurrencyString(currId, currencyString);

    for (let i = 0; i < selector.length; i++) {
        selector[i].innerHTML = currencyString;
    }

    go(`${config.rootUrl}/user/getByType/${currId}`, "GET", {
        currId: document.getElementById("currType").value
    })
        .then(totals => {

            for (const [clave, valor] of Object.entries(totals)) {
                document.getElementById("type-" + clave.toString()).innerHTML = valor.toString() + currencyString;
                console.log(`La clave ${clave} tiene el valor ${valor}`);
            }
        });
});

// Cargar imagen de perfil
window.addEventListener("load", (event) => {
    console.log("page is fully loaded");

    document.getElementById('img-profile').addEventListener('click', function () {
        document.getElementById('avatar').click();
    });

    document.getElementById('avatar').addEventListener("change", function (e) {
        console.log("change detected");

        var reader = new FileReader();
        reader.onload = function (e) {
            // get loaded data and render thumbnail.
            document.getElementById("img-profile").src = e.target.result;
            document.getElementById("nav-profile").src = e.target.result;
        };
        // read the image file as a data URL.
        reader.readAsDataURL(this.files[0]);
    });
});

function getCurrencyString(valueSelected, currencyString) {
    switch (valueSelected) {
        case 0:
            currencyString = "€";
            break;
        case 1:
            currencyString = "$";
            break;
        case 2:
            currencyString = "£";
            break;
        default:
            currencyString = " ";
            break;
    }
    return currencyString;
}

// FECHA DE LOS GASTOS DEL MES NO SEA SUPERIOR A LA ACTUAL
// Obtener la fecha actual
const today = new Date();

// Establecer la fecha máxima
const maxDate = new Date(today.getFullYear(), today.getMonth() + 1);

// Convertir la fecha a formato ISO para establecer como valor de "max" del input
const maxDateISO = maxDate.toISOString().slice(0, 7);

// Establecer la fecha máxima en el campo de entrada
const myMonthInput = document.getElementById("date");
myMonthInput.setAttribute("max", maxDateISO);