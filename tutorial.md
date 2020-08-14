# Title

> Start from scratch and time yourself following your own tutorial. Double the time it took and that's the "estimated build time". Link to the github repo

⏱ **60 minutes build time || [Github Repo](https://github.com/team-telnyx/demo-conference-php)**

## Introduction

> Add a 2-3 sentence paragraph describing the product & the product feature that the tutorial is showing off.

>  Example below⚠️ Be sure to update for your tutorial

```
The [Call Control framework](/docs/api/v2/call-control) is a set of APIs that allow complete control of a call flow from the moment a call begins to the moment it is completed. In between, you will receive a number of [webhooks](/docs/v2/call-control/receiving-webhooks) for each step of the call, allowing you to act on these events and send commands using the Telnyx Library. A subset of the operations available in the Call Control API is the [Call Control Conference](/docs/api/v2/call-control/Conference-Commands) API. This allows the user (you) to create and manage a conference programmatically upon receiving an incoming call, or when initiating an outgoing call.
```

## What you can do

> Add a few bullet points describing the application and show of the key points to understand. The "what you can do" should be "glance-able" high-level features important to understand.

>Example below:

```
At the end of this tutorial you'll have an application that:

* Verifies inbound webhooks are indeed from Telnyx
* Creates a conference for the first caller
* Adds additional callers to the existing conference
* Tears down the conference when the last call leaves
* Will create a new conference when the next caller dials in
```

## Pre-reqs & technologies

> At this point; the reader should understand what they're about to follow along with. We want them to understand what the tutorial is about **before** we dive into pre-reqs & technologies.

> The pre-reqs & technologies are the 'assumptions' you have about the reader.  These can be as complex or as simple as needed... ⭐️ Each pre-req should **ALSO** contain a link for the user to learn more.  The link can be to another quickstart or tutorial with the same concept. Any given reader should be able to start at the "top" ▲ and click all the way down to _"how to install NODE"_.

>Example below

* [Telnyx Account](https://telnyx.com/sign-up)
* [Telnyx Phone Number](https://portal.telnyx.com/#/app/numbers/my-numbers) enabled with:
  * [Telnyx Call Control Application](https://portal.telnyx.com/#/app/call-control/applications)
  * [Telnyx Outbound Voice Profile](https://portal.telnyx.com/#/app/outbound-profiles)
* [PHP](https://developers.telnyx.com/docs/v2/development/dev-env-setup?lang=php) installed with [Composer](https://getcomposer.org/)
* [Familiarity with Slim](http://www.slimframework.com/)
* Ability to receive webhooks (with something like [ngrok](https://developers.telnyx.com/docs/v2/development/ngrok))

## Setup

> At this point the reader should understand what they're building, What tools they'll be using to build, and have the material to learn more.

> The setup section covers the pre-code environment setup. Basically, what needs to be done before writing the first line of code. Usually covers package installation, ngrok config, Telnyx portal activities.

### Telnyx Portal configuration

> Example for call control application

You’ll need set up a Mission Control Portal account, buy a number and connect that number to a [Call Control Application](https://portal.telnyx.com/#/app/call-control/applications). You can learn how to do that in the quickstart guide [here](/docs/v2/call-control/quickstart).

The [Call Control Application](https://portal.telnyx.com/#/app/call-control/applications) needs to be setup to work with the conference control api:

* make sure the *Webhook API Version* is **API v2**

* Fill in the *Webhook URL* with the address the server will be running on. Alternatively, you can use a service like [Ngrok](/docs/v2/development/ngrok) to temporarily forward a local port to the Internet to a random address and use that. We'll talk about this in more detail later.

Finally, you need to create an [API Key](https://portal.telnyx.com/#/app/auth/v2) - make sure you save the key somewhere safe.

### Install packages via composer

> PHP Example Below, the exact commands will differ between programming languages

```shell
composer require slim/slim:^4.0
composer require slim/http
composer require slim/psr7
composer require telnyx/telnyx-php
composer require vlucas/phpdotenv
```

This will create `composer.json` file with the packages needed to run the application.

### Setting environment variables

> ⚠️ Each app should use environment variables loaded from a `.env` file to save any API keys/creds. The `.env` should be added to the `.gitignore` file for each sample. We are encouraging our users to follow good security practices. The `.env` methodology follows the recommendation of the [12 factor app](https://12factor.net/config).

> The 'dotenv' package helper is the the only thing that should change between languages.
> * Node -> https://github.com/motdotla/dotenv
> * PHP -> https://github.com/vlucas/phpdotenv
> * Python -> https://github.com/theskumar/python-dotenv
> * Ruby -> https://github.com/bkeepers/dotenv
> * Java -> https://github.com/cdimascio/java-dotenv
> * C# -> https://github.com/bolorundurowb/dotenv.net

This tutorial uses the excellent **link to whichever package** package to manage environment variables.

Create a `.env` file in your root directory to contain your API & Public key. **BE CAREFUL TO NOT SHARE YOUR KEYS WITH ANYONE** Recommended to add `.env` to your `.gitignore` file.

Your `.env` file should look something like:

```
TELNYX_API_KEY="KEYABC123_ZXY321"
TELNYX_PUBLIC_KEY="+lorem/ipsum/lorem/ipsum="
```

## Code-along

> Here is the code explanation. This will differ between tutorials and apps. See examples on https://developers.telnyx.com/docs for inspiration

## Usage

> Now that the code is complete; how to actually run the application and demo!
> The actual steps will differ from tutorial to tutorial, the ngrok configuration here is great!

> Example PHP Usage

Start the server `php -S localhost:8000 -t public`

When you are able to run the server locally, the final step involves making your application accessible from the internet. So far, we've set up a local web server. This is typically not accessible from the public internet, making testing inbound requests to web applications difficult.

The best workaround is a tunneling service. They come with client software that runs on your computer and opens an outgoing permanent connection to a publicly available server in a data center. Then, they assign a public URL (typically on a random or custom subdomain) on that server to your account. The public server acts as a proxy that accepts incoming connections to your URL, forwards (tunnels) them through the already established connection and sends them to the local web server as if they originated from the same machine. The most popular tunneling tool is `ngrok`. Check out the [ngrok setup](/docs/v2/development/ngrok) walkthrough to set it up on your computer and start receiving webhooks from inbound messages to your newly created application.

> Note below is tutorial specific!

Once you've set up `ngrok` or another tunneling service you can add the public proxy URL to your Connection in the Mission Control Portal. To do this, click  the edit symbol [✎] next to your Connection. In the "Webhook URL" field, paste the forwarding address from ngrok into the Webhook URL field. Add `/Callbacks/Voice/Inbound` to the end of the URL to direct the request to the webhook endpoint in your slim-php server.

For now you'll leave “Failover URL” blank, but if you'd like to have Telnyx resend the webhook in the case where sending to the Webhook URL fails, you can specify an alternate address in this field.

