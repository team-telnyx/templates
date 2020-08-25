<div align="center">

# Telnyx Node {%tutorial-name%}

![Telnyx](logo-dark.png)

{%one-sentence-description%}

</div>

## Documentation & Tutorial

The full documentation and tutorial is available on `[developers.telnyx.com](https://developers.telnyx.com/docs/v2/../?lang=node&utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link)`

## Pre-Reqs

You will need to set up:

* [Telnyx Account](https://telnyx.com/sign-up?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link)
* [Telnyx Phone Number](https://portal.telnyx.com/#/app/numbers/my-numbers?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link) enabled with:
  * [Telnyx Call Control Application](https://portal.telnyx.com/#/app/call-control/applications?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link)
  * [Telnyx Outbound Voice Profile](https://portal.telnyx.com/#/app/outbound-profiles?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link)
* [NodeJS](https://developers.telnyx.com/docs/v2/development/dev-env-setup?lang=node&utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link) installed.
* Ability to receive webhooks (with something like [ngrok](https://developers.telnyx.com/docs/v2/development/ngrok?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link))

## What you can do

* A few detailed bullet points on what the app does

## Usage

The following environmental variables need to be set

| Variable            | Description                                                                                                                                              |
|:--------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TELNYX_API_KEY`    | Your [Telnyx API Key](https://portal.telnyx.com/#/app/api-keys?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link)              |
| `TELNYX_PUBLIC_KEY` | Your [Telnyx Public Key](https://portal.telnyx.com/#/app/account/public-key?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link) |
| `TELNYX_APP_PORT`   | **Defaults to `8000`** The port the app will be served                                                                                                   |

### .env file

This app uses the excellent [dotenv](https://www.npmjs.com/package/dotenv) package to manage environment variables.

Make a copy of [`.env.sample`](./.env.sample) and save as `.env` **📁 in the root directory** and update the variables to match your creds.

```
TELNYX_API_KEY=
TELNYX_PUBLIC_KEY=
TENYX_APP_PORT=8000
```

### Callback URLs For Telnyx Applications

| Callback Type                    | URL                                        |
|:---------------------------------|:-------------------------------------------|
| Inbound Voice Callback           | `{ngrok-url}/callbacks/voice/inbound`      |
| Inbound Message Callback         | `{ngrok-url}/callbacks/messaging/inbound`  |
| Outbound Message Status Callback | `{ngrok-url}/callbacks/messaging/outbound` |

### Install

Run the following commands to get started

```
$ git clone {}
$ npm i
```

### Ngrok

This application is served on the port defined in the runtime environment (or in the `.env` file). Be sure to launch [ngrok](https://developers.telnyx.com/docs/v2/development/ngrok?utm_source=referral&utm_medium=github_referral&utm_campaign=cross-site-link) for that port

```
./ngrok http 8000
```

> Terminal should look _something_ like

```
ngrok by @inconshreveable                                                                                                                               (Ctrl+C to quit)

Session Status                online
Account                       Little Bobby Tables (Plan: Free)
Version                       2.3.35
Region                        United States (us)
Web Interface                 http://127.0.0.1:4040
Forwarding                    http://your-url.ngrok.io -> http://localhost:8000
Forwarding                    https://your-url.ngrok.io -> http://localhost:8000

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00
```

At this point you can point your callbacks application to generated ngrok URL + path  (Example: `http://{your-url}.ngrok.io/Callbacks/Voice/Inbound`).

### Run

Run the app with the NPM

```
$ npm run
```

## Next Steps

