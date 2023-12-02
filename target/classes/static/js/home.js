const groupsTable = document.getElementById("groupsTable");
const userId = groupsTable.dataset.userid;

function getGroups() {
    go(`${config.rootUrl}/user/getGroups`, "GET")
        .then(groups => {
            groupsTable.insertAdjacentHTML("afterbegin",`<h2 id="group-none" style="display: none; text-align: center; font-size: 16px; margin-top: 20px; text-transform: uppercase; letter-spacing: 2px;">You don't have groups yet</h2>`);
            const e = document.getElementById('group-none');

            if(groups.length == 0)
                e.style.display = 'block'
            else{
                Array.from(groups).forEach(group => {
                    const elem = document.getElementById(`group-${group.id}`);
                    if (elem != null)
                        groupsTable.removeChild(elem);
                    const member = group.members.find(member => member.idUser == userId);
                    if (member != null && member.enabled)
                        groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, member.balance));
                })
            }           
        })
        .catch(e => {
            console.log("Error retrieving group", e);
        })
}

// Render current groups
getGroups();

// Render INCOMING changes
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        if (obj.type == "GROUP") {
            const group = obj.group;
            const elem = document.getElementById(`group-${group.id}`);
            if (elem != null)
                elem.parentElement.removeChild(elem);
            if (obj.action != "GROUP_DELETED") {
                const member = group.members.find(member => member.idUser == userId);
                if (member != null && member.enabled)
                    groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, member.balance));
            }
            
            // Render mensaje no existen grupos
            const e = document.getElementById('group-none');
            if (e.style.display === 'none' && groupsTable.childElementCount == 2) // El 2 es el boton de + y el mensaje de vacio
                e.style.display = 'block';  // Mostrar el elemento
            else if(groupsTable.childElementCount > 2)
                e.style.display = 'none';  // Ocultar el elemento
        }

        else if (obj.type === "EXPENSE") {
            // get groupId from destination
            const expGroupId = parseInt(destination.split("/")[3]);
            const elem = document.getElementById(`group-${expGroupId}`);
            go(`${config.rootUrl}/group/${expGroupId}/getGroupConfig`, "GET")
                .then(group => {
                    elem.parentElement.removeChild(elem);
                    const member = group.members.find(member => member.idUser == userId);
                    if (member)
                        groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, member.balance));
                })
        }
    }
}

// Render group
function renderGroup(group, balance) {
    const truncatedBalance = Number(balance).toFixed(2);
    return `<div id="group-${group.id}" class="col">
                <div class="card m-2 text-white" role="button" onclick="location.href='/group/${group.id}'" tabindex="0">
                    <div class="card-header">
                        <h5>${group.name}</h5>
                    </div>
                    <div class="card-body">
                        <div class="row height">
                            <div>${group.desc}</div>
                        </div>
                        <div class="row">
                        <div class="balance col ms-3">
                            <span class="dot" style="${balance >= 0 ? 'background: green' : 'background: red'}"></span>
                            ${truncatedBalance}${group.currencyString}                                
                         </div>
                            <div class="col me-3 col-num-members">
                                <div class="icon">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" class="bi bi-person-fill" viewBox="0 0 16 16">
                                        <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3Zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                                    </svg>
                                </div>
                                <div class="num-members">${group.numMembers}</div>
                        </div>
                        </div>
                    </div>
                </div>
            </div>`
}

