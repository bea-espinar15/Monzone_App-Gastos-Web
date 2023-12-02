# Equipo Monzone - Proyecto (Pre-Examen)

## Propuesta
Monzone es la solución perfecta para dividir gastos entre amigos, familiares o compañeros de piso. Con esta aplicación web se pueden crear
grupos de gastos, agregar compras y dejar que la aplicación calcule de forma automática y eficiente las deudas entre cada miembro del grupo
para reducir el número de pagos. Además, con esta plataforma el usuario podrá configurar un presupuesto en cada grupo y recibir avisos cuando
se acerque a su límite establecido, así como visualizar estadísticas de diversos tipos sobre los gastos en los que va participando.

### Funcionalidades
Seguidamente se proporciona una lista de las funcionalidades principales que ofrece Monzone al usuario:
- Crear un grupo -> Cualquier usuario puede crear un grupo con la configuración deseada, convirtiéndose automáticamente en el moderador de dicho grupo
- Editar/Eliminar un grupo -> Si eres el moderador, podrás editar los ajustes de tu grupo o eliminarlo
- Invitar/Quitar miembros -> Si eres el moderador, podrás invitar a cualquier usuario, siempre que conozcas su nombre de usuario, y eliminar a miembros
que no tengan deudas (a favor o en contra)
- Añadir/Editar/Eliminar gastos en un grupo -> Cualquier miembro del grupo podrá gestionar los gastos, incluyendo el usuario que lo pagó, quiénes participaron
en el gasto y la categoría en la que lo incluyen, entre otros muchos aspectos
- Visualizar los gastos y las deudas -> Cualquier miembro del grupo podrá ver todos los gastos que se han ido añadiendo, así como una representación visual
del equilibrio que tiene cada miembro y las deudas calculadas por la app
- Marcar una deuda como pagada -> Cualquier miembro del grupo podrá marcar una deuda como pagada, que se registrará a modo de "gasto positivo", para restaurar
el equilibro de los usuarios
- Editar perfil -> Por supuesto, el usuario podrá modificar su información personal, incluyendo el cambio de contraseña
- Visualizar estadísticas personales -> Cada usuario tendrá acceso a unas estadísticas que le permitirán conocer su total de gastos en un mes particular,
así como el dinero que ha gastado en cada una de las categorías disponibles. Todo esto en el tipo de moneda que seleccione
- Ver resumen de grupos -> Además, en el perfil podrá ver rápidamente una lista de los grupos a los que pertenece, con la información más relevante
- Notificaciones -> Por un lado, se informará a los usuarios cuando otro haga cualquier tipo de modificación en un grupo (incluyendo cambios en gastos,
cambio en los ajustes del grupo, nuevos miembros que llegan...). Además, también se notificará al usuario cuando llegue al 50%, 75% y 100% de su presupuesto 
en alguno de sus grupos. Estas notificaciones podrán marcarse como leídas o directamente eliminarse si el usuario no desea conservarlas
- Invitaciones -> Un usuario podrá aceptar o denegar una invitación a un grupo
- Admin -> Los administradores de la aplicación podrán tener acceso (de lectura) a toda la información almacenada en la aplicación, para poder detectar
anomalías o acciones sospechosas

## Diagrama BD
A continuación se listan las tablas que forman la Base de Datos de Monzone, con los atributos que contienen.
- User:
  - id - PK
  - enabled - Para bajas lógicas
  - name - Nombre
  - username - Nombre de usuario, único
  - password - Contraseña cifrada
  - roles - Lista de roles, puede ser {User,Admin}
- Group:
  - id - PK
  - enabled - Para bajas lógicas
  - name - Nombre
  - desc - Descripción
  - num_members - Número de miembros, atributo derivado (count(members))
  - tot_budget - Presupuesto grupal, atributo derivado (suma de los budget de cada miembro)
  - currency - Tipo de moneda, uno de {EUR,USD,GBP}
- Member:
  - user_id - Usuario que es miembro, PK
  - group_id - Grupo al que pertenece, PK
  - enabled - Para bajas lógicas
  - role - Rol del usuario en el grupo, puede ser uno de estos {User,Moderator}
  - budget - Presupuesto del miembro en ese grupo
  - balance - Dinero total que debe/le deben en un momento al miembro
- Debt:
  - group_id - Grupo en el que se da la deuda, PK
  - debtor_id - Usuario que debe dinero, PK
  - debt_owner_id - Usuario al que le deben dinero, PK
  - amount - Cuánto debe debtor a debt_owner
- Type:
  - id - PK
  - name - Nombre identificador de la categoría
- Expense:
  - id - PK
  - enabled - Para bajas lógicas
  - name - Nombre
  - desc - Descripción
  - amount - Cuánto ha costado el gasto
  - type_id - Categoría del gasto
  - date - Fecha en la que se produjo el gasto
  - paid_by_id - Usuario que pagó el gasto
- Participates:
  - expense_id - Gasto, PK
  - group_id - Grupo al que pertenece el gasto
  - user_id - Usuario que participa en el gasto, PK
- Notification:
  - id - PK
  - date_read - Fecha en la que se leyó la notificación
  - date_sent - Fecha en la que se envió la notificación
  - group_id - Grupo implicado en la notificación
  - type - Tipo de notificación (ver clase Notification.java)
  - message - Mensaje que contiene la notificación
  - recipient_id - Usuario destinatario
  - sender_id - Usuario emisor

## Usuarios y Roles
### Usuarios
En primer lugar, con el fichero import.sql se crean en la BD los siguientes usuarios, con sus correspondientes contraseñas:
- Username: a | Name: admin | Password: aa
- Username: b | Name: bonito | Password: aa
- Username: Nico | Name: Nicoooooo | Password: aa
- Username: Tester | Name: Tester | Password: aa
- Username: user4 | Name: User 4 | Password: aa
- Username: user5 | Name: User 5 | Password: aa
- Username: user6 | Name: User 6 | Password: aa
- Username: user7 | Name: User 7 | Password: aa
- Username: user8 | Name: User 8 | Password: aa
- Username: user9 | Name: User 9 | Password: aa
- Username: user10 | Name: User 10 | Password: aa
- Username: user11 | Name: User 11 | Password: aa
- Username: user12 | Name: User 12 | Password: aa
- Username: user13 | Name: User 13 | Password: aa

### Roles
En cuanto a tipos de usuario general, existen dos: USER y ADMIN. El usuario normal (USER) es aquel que puede utilizar la funcionalidad normal de la aplicación, formando parte de
grupos, gestionando gastos y deudas.. El administrador (ADMIN) tiene una funcionalidad extra, que permite visualizar todas las entidades que forman parte de la BD. Puede ver
una lista de todos los usuarios (activos o no) con su información (excepto contraseña, evidentemente), y una lista de grupos (activos o no) con su información, incluyendo los miembros
que forman/formaban parte de él, los gastos y las deudas que contiene.
-> En este caso, los usuarios administradores son a (admin) y Nico (Nicoooooo)

Por otro lado, en el contexto de la aplicación, existen a su vez dos maneras de "pertenecer" a un grupo: siendo usuario normal (USER) o siendo moderador (MODERATOR) del grupo.
El primero puede visualizar todo el contenido del grupo, así como crear/editar/borrar gastos, saldar deudas o editar su presupuesto personal dentro del grupo (también puede salirse
del grupo). El moderador, además de estas funcionalidades, tiene unos privilegios extra sobre el grupo: puede editar la configuración del grupo, invitar a miembros y eliminarlos,
y eliminar el grupo.

## Pruebas
Para la realización de pruebas se ha creado un usuario "Tester" que no pertenece a ningún grupo y en el usuario "b" se han añadido un grupo y un expense de forma permanente.
En cuanto a la implementación, se han elaborado 3 archivos .features para llevar a cabo las pruebas:

- login.feature, contiene escenarios relacionados con el inicio y fin de sesión:
    - Hacer login con distintos usuarios (entre ellos el admin)
    - Hacer logout

- principal.feature, contiene los escenarios básicos de navegación:
    - Entrar a un grupo
    - Entrar a la configuración de un grupo
    - Entrar en un gasto
    - Entrar en el perfil
        
- usage.feature, contiene los escenarios básicos de uso de la aplicación:
    - Crear un nuevo grupo y comprobar que se ha creado correctamente
	  - Eliminar un gasto
    - Crear un gasto y comprobar que se ha creado correctamente
    - Invitar a un usuario a unirse a un grupo, unirse y entrar en el grupo

## Comentarios
### Cosas que han quedado sin implementar
Hay algunas mejoras que no ha sido posible implementar para esta entrega, aunque en el fichero TODO.md se incluyen todas las 
correcciones que sí se han hecho con respecto a la entrega anterior:
- Añadir orden a los grupos en home (por nombre) y en admin (por ID).
- No se han añadido transacciones de deudas en la BD porque ya se guarda registro de los pagos que se hacen a través de los 
Reimbursement (creados en el servidor cuando un usuario paga una deuda).
- Un Reimbursement no debería poder editarlo el usuario.

### Otros comentarios
- Además de la carpeta src, donde se incluye el código fuente, el pom.xml y el README.md, existe un directorio doc/ donde se incluye
documentación extra utilizada durante el proyecto:
  - El modelo de datos, elaborado con la herramienta de modelado de software IBM RSAD
  - El modelo del dominio (Diagrama Entidad-Relación), elaborado con Lucid
  - Una "especificación de requisitos software", que contiene el diseño de cada interfaz, con los requisitos importantes a tener en cuenta
  para cada funcionalidad (NOTA: No está actualizada, puede contener requisitos obsoletos)
  - TODO.md -> Fichero donde se incluyen las funcionalidades implementadas, las mejoras que se han realizado para la entrega antes del 
  examen de la asignatura, y los aspectos que han quedado sin implementar, junto con la prioridad de cada tarea
  - TODO - Excel Completo -> Excel realizado antes de la entrega pre-examen para organizar la asignación de tareas
  - 4 ficheros con el diseño de marca de la aplicación: el logo original, el logo en sus dos versiones (light mode VS dark mode) y el icono
- Se incluye aquí el [link al repositorio de GitHub](https://github.com/Nicolas-EM/IW-Monzone.git)