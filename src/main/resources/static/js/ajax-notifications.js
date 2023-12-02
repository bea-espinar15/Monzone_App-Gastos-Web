function getTimeAgo(dateString) {
    const date = new Date(dateString);

    const millisecondsPerMinute = 60000;
    const millisecondsPerHour = 3600000;
    const millisecondsPerDay = 86400000;
    const millisecondsPerWeek = 604800000;
    const millisecondsPerMonth = 2592000000;
    const millisecondsPerYear = 31536000000;

    const currentDate = new Date();

    // Calculate the difference in milliseconds between the current date and the given date
    const differenceInMilliseconds = currentDate - date;

    if (differenceInMilliseconds < 2 * millisecondsPerMinute) {
        return 'Just now';  // Less than 2 minutes ago
    } else if (differenceInMilliseconds < millisecondsPerHour) {
        const minutes = Math.floor(differenceInMilliseconds / millisecondsPerMinute);
        return minutes + ' minutes ago';
    } else if (differenceInMilliseconds < millisecondsPerDay) {
        const hours = Math.floor(differenceInMilliseconds / millisecondsPerHour);
        return hours === 1 ? '1 hour ago' : hours + ' hours ago';
    } else if (differenceInMilliseconds < millisecondsPerWeek) {
        const days = Math.floor(differenceInMilliseconds / millisecondsPerDay);
        return days === 1 ? '1 day ago' : days + ' days ago';
    } else if (differenceInMilliseconds < millisecondsPerMonth) {
        const weeks = Math.floor(differenceInMilliseconds / millisecondsPerWeek);
        return weeks === 1 ? '1 week ago' : weeks + ' weeks ago';
    } else if (differenceInMilliseconds < millisecondsPerYear) {
        const months = Math.floor(differenceInMilliseconds / millisecondsPerMonth);
        return months === 1 ? '1 month ago' : months + ' months ago';
    } else {
        const years = Math.floor(differenceInMilliseconds / millisecondsPerYear);
        return years === 1 ? '1 year ago' : years + ' years ago';
    }
}

function renderReadNotif(notif) {
    return `<div id="notif-${notif.id}" class="row my-2">
                <div class="card text-white" role="button">
                    <div class="card-body">
                        <div class="row">
                            <h5>${notif.message}</h5>
                        </div>
                        <div class="row">
                            <p>${notif.dateSent.substring(0, 10)} (${getTimeAgo(notif.dateSent)})</p>
                        </div>
                        <div class="row mt-2">
                            <div class="col invisible">
                            </div>
                            <form class="col" method="post" action="/user/${notif.id}/delete">
                                <button class="btn btn-delete btn-primary rounded-pill fw-bold" onclick="deleteNotif(event, this, ${notif.id})">Delete</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>`
}

function renderUnreadNotif(notif) {
    return `<div id="notif-${notif.id}" class="row my-2">
                <div class="card text-white bg-info" role="button">
                    <div class="card-body">
                        <div class="row">
                            <h5>${notif.message}</h5>
                        </div>
                        <div class="row">
                            <p>${notif.dateSent.substring(0, 10)} (${getTimeAgo(notif.dateSent)})</p>
                        </div>
                        <div class="row mt-2">
                            <form id="notifReadBtn-${notif.id}" class="col" method="post" action="/user/${notif.id}/read">
                                <button class="btn btn-func btn-primary rounded-pill fw-bold" onclick="markNotifRead(event, this, ${notif.id})" type="submit">Mark Read</button>
                            </form>
                            <form class="col" method="post" action="/user/${notif.id}/delete">
                                <button class="btn btn-delete btn-primary rounded-pill fw-bold" onclick="deleteNotif(event, this, ${notif.id})">Delete</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>`
}

function renderInvitation(notif) {
    return `<div id="notif-${notif.id}" class="row my-2">
                <div class="card text-white bg-info" role="button">
                    <div class="card-body">
                        <div class="row">
                            <h5>${notif.message}</h5>
                        </div>
                        <div class="row">
                            <p>${notif.dateSent.substring(0, 10)} (${getTimeAgo(notif.dateSent)})</p>
                        </div>
                        <div class="row mt-2">
                            <form id="notifReadBtn-${notif.id}" class="col" method="post" action="/group/${notif.idGroup}/acceptInvite">
                            <button id="btn-accept" class="btn btn-func btn-primary rounded-pill fw-bold" onclick="acceptInvite(event, this, ${notif.id})">Accept</button>
                            </form>
                            <form class="col" method="post" action="/user/${notif.id}/delete">
                                <button class="btn btn-delete btn-primary rounded-pill fw-bold" onclick="deleteNotif(event, this, ${notif.id})">Delete</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>`
}

// pinta notifs viejos al cargarse, via AJAX
go(config.rootUrl + "/user/receivedNotifs", "GET")
    .then(notifs => {
        const actionNotifsDiv = document.getElementById("actionNotifs-tab-pane");
        const notifsDiv = document.getElementById("notifs-tab-pane");

        notifsDiv.insertAdjacentHTML("beforeend", `<h3 id="notif-none" style="display: none;">You don't have notifications yet</h3>`);
        actionNotifsDiv.insertAdjacentHTML("beforeend", `<h3 id="invit-none" style="display: none;">You don't have invitations yet</h3>`);
        
        const noNotif = document.getElementById('notif-none');
        const noInvit = document.getElementById('invit-none');
       
        notifs.forEach(notif => {
            if (notif.type == "GROUP_INVITATION") {
                actionNotifsDiv.insertAdjacentHTML("beforeend", renderInvitation(notif));
            } else {
                if (notif.dateRead === "") {
                    notifsDiv.insertAdjacentHTML("beforeend", renderUnreadNotif(notif));
                }
                else {
                    notifsDiv.insertAdjacentHTML("beforeend", renderReadNotif(notif));
                }
            }
        })

        if(notifsDiv.childElementCount == 1)
            noNotif.style.display = 'block'

        if(actionNotifsDiv.childElementCount == 1)
            noInvit.style.display = 'block'
    }
    );

// y aquí pinta notificaciones según van llegando
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        if (obj.type == "NOTIFICATION") {
            console.log("Received notification");
            let p = document.querySelector("#nav-unread");
            if (p) {
                p.textContent = +p.textContent + 1;
            }

            let notif = obj.notification;
            let actionNotifsDiv = document.getElementById("actionNotifs-tab-pane");
            let notifsDiv = document.getElementById("notifs-tab-pane");

            if (notif.type == 'GROUP_INVITATION') {
                actionNotifsDiv.insertAdjacentHTML("afterbegin", renderInvitation(notif));

                renderNoInvitations();
            }
            else {
                notifsDiv.insertAdjacentHTML("afterbegin", renderUnreadNotif(notif));

                renderNoNotif(); 
            }

            createToastNotification(notif.id, notif.message);
        }
    }
}

function renderNoNotif() {
    let notifsDiv = document.getElementById("notifs-tab-pane");
    const e = document.getElementById('notif-none');
    if (e.style.display === 'none' && notifsDiv.childElementCount == 1)
        e.style.display = 'block'; // Mostrar el elemento
    else if (notifsDiv.childElementCount > 1)
        e.style.display = 'none'; // Ocultar el elemento  
}

function renderNoInvitations() {
    let actionNotifsDiv = document.getElementById("actionNotifs-tab-pane");
    const e = document.getElementById('invit-none');
    if (e.style.display === 'none' && actionNotifsDiv.childElementCount == 1)
        e.style.display = 'block'; // Mostrar el elemento
    else if (actionNotifsDiv.childElementCount > 1)
        e.style.display = 'none';
}


// Accept Invite Btn
function acceptInvite(event, btn, notifId) {
    event.preventDefault();
    go(btn.parentNode.action, 'POST', {})
        .then(d => {
            console.log("Invite accepted", d.status);
            ws.subscribe(`/topic/group/${d.id}`);
            deleteClientNotif(notifId);
            createToastNotification(notifId, "Invitation Accepted");
            // document.getElementById('offcanvasNav').hide();

            renderNoInvitations();
        })
        .catch(e => console.log("sad", e))
}

// Mark notif as read
function markNotifRead(event, btn, notifId) {
    event.preventDefault();
    go(btn.parentNode.action, 'POST', {})
        .then(d => {
            document.getElementById(`notifReadBtn-${notifId}`).classList.add('invisible');

            let cardContainer = document.getElementById(`notif-${notifId}`);
            let card = cardContainer.querySelector(".card");
            card.classList.remove("bg-info");

            let p = document.querySelector("#nav-unread");
            if (p) {
                console.log("ENTRA: markNotifRead");
                p.textContent = +p.textContent - 1;
            }
        })
        .catch(e => console.log("Failed to mark notif as read", e))
}

// Unsuscribe from group
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj);
        if (obj.type == "GROUP") {
            const member = obj.group.members.find(member => member.idUser == userId);
            // You're the one removed
            if (member != null && !member.enabled)
                ws.unsubscribe(`/topic/group/${obj.group.id}`)
        }
    }
}

// Delete notif client side
function deleteClientNotif(notifId) {
    // Restar según si se pulso el boton de leer o no
    let notifBtn = document.getElementById(`notifReadBtn-${notifId}`);
    let p = document.querySelector("#nav-unread");

    if (notifBtn && !notifBtn.classList.contains('invisible'))
        p.textContent = +p.textContent - 1;
    else if (!notifBtn)
        p.textContent = +p.textContent - 1;

    const notifDiv = document.getElementById(`notif-${notifId}`);
    notifDiv.parentElement.removeChild(notifDiv);
}

// Delete notif server side
function deleteNotif(event, btn, notifId) {
    event.preventDefault();
    go(btn.parentNode.action, 'POST', {})
        .then(d => {
            deleteClientNotif(notifId);
            createToastNotification(notifId, "Notification Deleted");
            
            renderNoInvitations();
            renderNoNotif();
        })
        .catch(e => console.log("sad", e))
}

function createToastNotification(notifId, body, error = false) {
    if (error) {
        document.getElementById('toastNotifBar').insertAdjacentHTML("afterbegin", renderToastError(notifId, body));
    } else {
        document.getElementById('toastNotifBar').insertAdjacentHTML("afterbegin", renderToastNofif(notifId, body));
    }

    const toastNotif = bootstrap.Toast.getOrCreateInstance(document.getElementById(`toast-${notifId}`));
    toastNotif.show();
}

function renderToastNofif(notifId, body) {
    return `<div id="toast-${notifId}" class="toast bg-dark" role="alert" aria-live="assertive" aria-atomic="true" role="button" data-bs-toggle="offcanvas" data-bs-target="#offcanvasNav" aria-controls="offcanvasNav">
                <div class="toast-header bg-dark">
                    <img src="/img/icon.png" class="rounded me-2" alt="Monzone Icon" width="auto" height="50">
                    <strong class="me-auto text-light">Monzone</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body text-light">
                    ${body}
                </div>
            </div>`
}

function renderToastError(notifId, body) {
    return `<div id="toast-${notifId}" class="toast bg-danger" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header bg-danger">
                    <img src="/img/icon.png" class="rounded me-2" alt="Monzone Icon" width="auto" height="50">
                    <strong class="me-auto text-light">Monzone</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body text-light">
                    ${body}
                </div>
            </div>`
}