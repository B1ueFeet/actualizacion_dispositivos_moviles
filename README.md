# FreshGuard

FreshGuard es una aplicacion Android para llevar un inventario de productos del hogar y controlar sus fechas de caducidad

La idea principal es evitar el desperdicio de comida mostrando que productos estan frescos, cuales necesitan cuidado y cuales ya estan urgentes

## Que hace la app

- Registrar productos con nombre, cantidad, unidad, tipo y fecha de caducidad
- Mostrar los productos en una lista con RecyclerView
- Ordenar los productos por fecha de caducidad
- Mostrar un resumen con productos frescos, en cuidado y urgentes
- Usar colores tipo semaforo para saber rapido el estado de cada producto
- Actualizar un producto deslizando hacia la derecha
- Eliminar un producto deslizando hacia la izquierda
- Confirmar antes de borrar un producto
- Guardar los datos localmente con Room
- Programar notificaciones con AlarmManager
- Probar notificaciones con un boton de debug
- Mostrar una pantalla inicial con el logo de FreshGuard
- Usar colores y logos de la marca FreshGuard

## Estados de los productos

- Frescos: productos que tienen mas de 7 dias antes de caducar
- Cuidado: productos que vencen entre 2 y 7 dias
- Urgentes: productos vencidos, que vencen hoy o que vencen manana

## Tecnologias usadas

- Kotlin
- XML
- View Binding
- Material Components
- RecyclerView
- Navigation Component con Fragments
- ViewModel
- LiveData
- Kotlin Flow
- Coroutines
- Room
- AlarmManager
- BroadcastReceiver

## Arquitectura

La app esta hecha con MVVM

```text
Fragment -> ViewModel -> Repository -> DAO -> Room
```

Los Fragments manejan la parte visual y los eventos de la pantalla

El ViewModel mantiene el estado y llama al repositorio usando Coroutines

El Repository separa la logica de datos del resto de la app

El DAO contiene las consultas de Room

Room guarda todo en una base local SQLite

## Base de datos

La base local se llama

```text
anti_desperdicio.db
```

Tiene dos tablas principales

```text
foods
food_types
```

La tabla `foods` guarda

```text
id
name
expirationDate
quantity
unit
typeId
```

La tabla `food_types` guarda

```text
id
name
```

Tambien se usa `FoodWithType` para relacionar cada producto con su tipo

## Flujo de la app

1. Primero aparece una pantalla de carga con el logo
2. Despues se abre la lista principal de productos
3. El boton `+` abre el formulario para agregar un producto
4. Al guardar, el producto se guarda en Room y se programa una alarma
5. Si se desliza a la derecha se abre la pantalla para actualizar
6. Si se desliza a la izquierda se muestra un dialogo para confirmar la eliminacion
7. Si se elimina un producto, tambien se cancela la alarma que tenia programada

## Notificaciones

La app usa `AlarmManager` para programar alertas locales

Cuando llega la alarma, Android llama a `ExpirationAlarmReceiver`

Ese receiver crea la notificacion y muestra el aviso de producto por vencer

En Android 13 o superior se pide permiso para mostrar notificaciones

## Marca visual

La app usa la marca FreshGuard

Colores principales

```text
Verde de marca: #359477
Menta principal: #9ED8C1
Verde profundo: #1F6A55
Fondo: #F6F7F7
Tarjetas: #FFFFFF
Texto: #1F2229
```

Tambien se agrego soporte para modo oscuro usando colores propios para que no se rompa el contraste

## Como ejecutar

Abrir el proyecto en Android Studio y ejecutar el modulo `app` en un emulador o celular

Tambien se puede compilar desde terminal con

```bash
./gradlew assembleDebug
```

En Windows

```bash
gradlew.bat assembleDebug
```

## Estado actual

El proyecto cumple con lo pedido en el PDF

- XML sin Compose
- View Binding
- RecyclerView
- Material Components
- Navigation Component con Fragments
- ViewModel
- LiveData y Flow
- Room
- Coroutines
- AlarmManager
- Notificaciones locales

Hilt no se agrego porque era opcional

En este proyecto se usa inyeccion manual creando la base de datos, el repositorio y el ViewModelFactory desde los Fragments
