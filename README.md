# Auto NASA Wallpaper

Uma aplicação Spring Boot que busca automaticamente a Imagem Astronômica do Dia (APOD) da API da NASA, processa a imagem e a define como o papel de parede do seu desktop no Ubuntu.

## Funcionalidades
- Busca a imagem APOD diária e a sua respectiva explicação.
- Utiliza **ImageMagick** (`convert`) para cortar a imagem na proporção 16:9 e adicionar o título como uma sobreposição de texto com 40% de opacidade.
- Integração nativa com **Ubuntu (GNOME)** via `gsettings` para definir automaticamente a nova imagem processada como papel de parede (suporta temas claro e escuro).
- Roda como uma aplicação `CommandLineRunner` de execução única, economizando memória em vez de rodar como um servidor contínuo.

## Pré-requisitos
- Java 17+
- ImageMagick (comando `convert` disponível no PATH)

## Configuração
Edite o arquivo `src/main/resources/application.properties` para adicionar sua chave da API da NASA caso a `DEMO_KEY` atinja o limite de taxa.

## Como Executar
Compile o projeto e rode o arquivo `.jar`:

```bash
./mvnw clean package -DskipTests
java -jar target/nasa-apod-0.0.1-SNAPSHOT.jar
```

Você pode adicionar o comando do `.jar` aos seus **Aplicativos de Inicialização** (Startup Applications) no Ubuntu para que a atualização do papel de parede ocorra automaticamente todos os dias ao ligar o PC.
