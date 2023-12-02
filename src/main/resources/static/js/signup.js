// submit
document.getElementById('signupForm').addEventListener("submit", (e) => {
    e.preventDefault();
    console.log('Creating user');
    const b = document.getElementById("btn-signup");
    const name = document.getElementById("name").value;
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const repeatPassword = document.getElementById("repeatPassword").value;
  
    if (password !== repeatPassword){
        document.getElementById("errorParagraph").innerHTML = "Passwords don't match.";
        return;
    }

    go(b.getAttribute('formaction'), 'POST', {
        name,
        username,
        password
    })
        .then(d => {
            console.log("Sign Up: success", d);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => {
            console.log("Error creating user", e);
            document.getElementById("errorParagraph").innerHTML = JSON.parse(e.text).message;
        })
});

