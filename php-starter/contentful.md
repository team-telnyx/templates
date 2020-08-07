⏱ **60 minutes build time || `[Github Repo](https://github.com/team-telnyx/demo-conference-php)**`

## Introduction

The [Call Control framework](/docs/api/v2/call-control) is a set of APIs that allow complete control of a call flow from the moment a call begins to the moment it is completed. In between, you will receive a number of [webhooks](/docs/v2/call-control/receiving-webhooks) for each step of the call, allowing you to act on these events and send commands using the Telnyx Library. A subset of the operations available in the Call Control API is the [Call Control Conference](/docs/api/v2/call-control/Conference-Commands) API. This allows the user (you) to create and manage a conference programmatically upon receiving an incoming call, or when initiating an outgoing call.

The [Telnyx PHP Library](https://github.com/team-telnyx/telnyx-php) is a convenient wrapper around the Telnyx REST API. It allows you to access and control call flows using an intuitive object-oriented library. This tutorial will walk you through creating a simple [Slim](http://www.slimframework.com/) server that allows you to create and manage a conference.

## Pre-Reqs



## What can you do

At the end of this tutorial you'll have an application that:

* Verifies inbound webhooks are indeed from Telnyx
* Creates a conference for the first caller
* Adds additional callers to the existing conference
* Tears down the conference when the last call leaves
* Will create a new conference when the next caller dials in

## Setup

Before beginning, please setup ensure that you have [composer](https://getcomposer.org/) installed.

### Install packages

```shell
composer require slim/slim:^4.0
composer require slim/http
composer require slim/psr7
composer require telnyx/telnyx-php
composer require vlucas/phpdotenv
```

This will create `composer.json` file with the packages needed to run the application.

You’ll need set up a Mission Control Portal account, buy a number and connect that number to a [Call Control Application](https://portal.telnyx.com/#/app/call-control/applications). You can learn how to do that in the quickstart guide [here](/docs/v2/call-control/quickstart).

The [Call Control Application](https://portal.telnyx.com/#/app/call-control/applications) needs to be setup to work with the conference control api:

* make sure the *Webhook API Version* is **API v2**

* Fill in the *Webhook URL* with the address the server will be running on. Alternatively, you can use a service like [Ngrok](/docs/v2/development/ngrok) to temporarily forward a local port to the internet to a random address and use that. We'll talk about this in more detail later.

Finally, you need to create an [API Key](https://portal.telnyx.com/#/app/auth/v2) - make sure you save the key somewhere safe.

#### Setting environment variables

This tutorial uses the excellent [phpenv](https://github.com/vlucas/phpdotenv) package to manage environment variables.

Create a `.env` file in your root directory to contain your API & Public key. **BE CAREFUL TO NOT SHARE YOUR KEYS WITH ANYONE** Recommended to add `.env` to your `.gitignore` file.

Your `.env` file should look something like:

```
TELNYX_API_KEY="KEYABC123_ZXY321"
TELNYX_PUBLIC_KEY="+lorem/ipsum/lorem/ipsum="
```

## Code-along

Now create a folder `public` and a file in the public folder`index.php`, then write the following to setup the telnyx library.

```shell
mkdir public
touch public/index.php
```

#### Setup Slim Server and instantiate Telnyx


```php
<?php

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Server\RequestHandlerInterface as RequestHandler;
use Slim\Factory\AppFactory;
use Telnyx;

require __DIR__ . '/../vendor/autoload.php';

$dotenv = Dotenv\Dotenv::createImmutable(__DIR__, '../.env');
$dotenv->load();

$TELNYX_API_KEY    = $_ENV['TELNYX_API_KEY'];
$TELNYX_PUBLIC_KEY = $_ENV['TELNYX_PUBLIC_KEY'];
$CONFERENCE_FILE_NAME = '../conference_id.txt';

Telnyx\Telnyx::setApiKey($TELNYX_API_KEY);
Telnyx\Telnyx::setPublicKey($TELNYX_PUBLIC_KEY);

// Instantiate Slim App
$app = AppFactory::create();

// Add error middleware
$app->addErrorMiddleware(true, true, true);
```

📝 *Note* the `$CONFERENCE_FILE_NAME = '../conference_id.txt';` will be used to track conference state.

## Receiving Webhooks & Creating a Conference

Now that you have setup your auth token, phone number, and connection, you can begin to use the API Library to make and control conferences. First, you will need to setup a Slim endpoint to receive webhooks for call and conference events.

There are a number of webhooks that you should anticipate receiving during the lifecycle of each call and conference. This will allow you to take action in response to any number of events triggered during a call. In this example, you will use the `call.initiated`, `call.answered`, and `conference.ended` events to add calls to a conference and tear it down. Because you will need to wait until there is a running call before you can create a conference, plan to use call events to create the conference after a call is initiated.

### Basic Routing & Functions

The basic overview of the application is as follows:

1. Verify webhook & create TelnyxEvent
2. Check event-type and route to the event handler
3. `call.initiated` events are answered
4. `call.answered` events check if there is a conference, if so; join, if not, create new conference
5. `conference.ended` will tear down the existing conference making way for a new one.

#### Webhook validation middleware

Telnyx signs each webhook that can be validated by checking the signature with your public key. This example adds the verification step as middleware to be included on all Telnyx endpoints.  The [Webhooks Doc](https://developers.telnyx.com/docs/api/v2/overview#webhook-signing) elaborates more on how to check the headers and signature.

```php
//Callback signature verification
$telnyxWebhookVerify = function (Request $request, RequestHandler $handler) {
    //Extract the raw contents
    $payload = $request->getBody()->getContents();
    //Grab the signature
    $sigHeader = $request->getHeader('HTTP_TELNYX_SIGNATURE_ED25519')[0];
    //Grab the timestamp
    $timeStampHeader = $request->getHeader('HTTP_TELNYX_TIMESTAMP')[0];
    //Construct the Telnyx event which will validate the signature and timestamp
    $telnyxEvent = \Telnyx\Webhook::constructEvent($payload, $sigHeader, $timeStampHeader);
    //Add the event object to the request to keep context for future middleware
    $request = $request->withAttribute('telnyxEvent', $telnyxEvent);
    //Send to next middleware
    $response = $handler->handle($request);
    //return response back to Telnyx
    return $response;
};
```

ℹ️ For more details on middleware see [Slim's documentation on Route Middleware](http://www.slimframework.com/docs/v4/objects/routing.html#route-middleware)


#### Conference Management

For each call, we need to check if there is already a conference. In a more sophisticated application this would typically be solved by a connection to any given data store. For this demo, we're managing the state in a file on disc `$CONFERENCE_FILE_NAME`.

```php
// Read the ID out of the file, if doesn't exist return FALSE
function readConferenceFile (String $CONFERENCE_FILE_NAME) {
    if (!file_exists($CONFERENCE_FILE_NAME)) {
        return FALSE;
    }
    else {
        $conferenceFile = fopen($CONFERENCE_FILE_NAME, 'r') or die("Unable to open file!");
        $fileConferenceId = fread($conferenceFile, filesize($CONFERENCE_FILE_NAME));
        return $fileConferenceId;
    }
}

// Create the conference Id file and write the ID to disc
function createConferenceFile (String $conferenceId, String $CONFERENCE_FILE_NAME) {
    $conferenceFile = fopen($CONFERENCE_FILE_NAME, 'w') or die ('Unable to open conference file');
    fwrite($conferenceFile, $conferenceId);
    fclose($conferenceFile);
    return $conferenceId;
};

// Delete the file; making way for a new conference to be created for next caller
function deleteConferenceFile (String $CONFERENCE_FILE_NAME){
    if (!file_exists($CONFERENCE_FILE_NAME)) {
        return;
    }
    if (!unlink($CONFERENCE_FILE_NAME)) {
        die ('Can not delete conference file');
    }
    return;
};
```

#### Event Handlers and switch

For each event (besides `call.initiated` we need to check the current state of the conference before making next steps)


```php

//Adds the given call to the conference
function addCallToConference (String $callControlId, String $conferenceId) {
    $conference = new Telnyx\Conference($conferenceId);
    $joinConferenceParameters = array(
        'call_control_id' => $callControlId
    );
    $conference->join($joinConferenceParameters);
};

// creates a conference and creates the conference state file
function createConference (String $callControlId, String $CONFERENCE_FILE_NAME) {
    $conferenceName = uniqid('conf-');
    $conferenceParameters = array(
        'call_control_id' => $callControlId,
        'name' => $conferenceName,
        'beep_enabled' => 'always'
    );
    $newConference = Telnyx\Conference::create($conferenceParameters);
    $conferenceId = $newConference->id;
    createConferenceFile($conferenceId, $CONFERENCE_FILE_NAME);
    return $conferenceId;
}

// Speaks to our caller then determines whether to create a new conference or add to existing
function handleAnswer (String $callControlId, String $CONFERENCE_FILE_NAME) {
    $speakParams = array(
        'payload' => 'joining conference',
        'voice' => 'female',
        'language' => 'en-GB'
    );
    $call = new Telnyx\Call($callControlId);
    $call->speak($speakParams);
    $existingConferenceId = readConferenceFile($CONFERENCE_FILE_NAME);
    if (!$existingConferenceId) {
        createConference($callControlId, $CONFERENCE_FILE_NAME);
    }
    else {
        addCallToConference($callControlId, $existingConferenceId);
    }
    return;
};

// Add route
$app->post('/Callbacks/Voice/Inbound', function (Request $request, Response $response) {
    global $CONFERENCE_FILE_NAME;
    // Get the parsed event from the request
    $telnyxEvent = $request->getAttribute('telnyxEvent');
    // Extract the relevant information
    $data = $telnyxEvent->data;
    // Only _really_ care about events right now
    if ($data['record_type'] != 'event') {
        return $response->withStatus(200);
    }
    $callControlId = $data->payload['call_control_id'];
    $event = $data['event_type'];
    switch ($event) {
        case 'call.initiated':
            // Create a new call object
            $call = new Telnyx\Call($callControlId);
            // Then answer it
            $call->answer();
            break;
        case 'call.answered':
            handleAnswer($callControlId, $CONFERENCE_FILE_NAME);
            break;
        case 'conference.ended':
            deleteConferenceFile($CONFERENCE_FILE_NAME);
        default:
            # other events less importante right now
            break;
    }
    // Let's play nice and return 200
    return $response->withStatus(200);
})->add($telnyxWebhookVerify);

// run the thing!
$app->run();
```

## Usage

Start the server `php -S localhost:8000 -t public`

When you are able to run the server locally, the final step involves making your application accessible from the internet. So far, we've set up a local web server. This is typically not accessible from the public internet, making testing inbound requests to web applications difficult.

The best workaround is a tunneling service. They come with client software that runs on your computer and opens an outgoing permanent connection to a publicly available server in a data center. Then, they assign a public URL (typically on a random or custom subdomain) on that server to your account. The public server acts as a proxy that accepts incoming connections to your URL, forwards (tunnels) them through the already established connection and sends them to the local web server as if they originated from the same machine. The most popular tunneling tool is `ngrok`. Check out the [ngrok setup](/docs/v2/development/ngrok) walkthrough to set it up on your computer and start receiving webhooks from inbound messages to your newly created application.

Once you've set up `ngrok` or another tunneling service you can add the public proxy URL to your Connection in the Mission Control Portal. To do this, click  the edit symbol [✎] next to your Connection. In the "Webhook URL" field, paste the forwarding address from ngrok into the Webhook URL field. Add `/Callbacks/Voice/Inbound` to the end of the URL to direct the request to the webhook endpoint in your slim-php server.

For now you'll leave “Failover URL” blank, but if you'd like to have Telnyx resend the webhook in the case where sending to the Webhook URL fails, you can specify an alternate address in this field.

## Complete Running Call Control Conference Application

The [github repo](https://github.com/team-telnyx/demo-conference-php) contains an extended version of the tutorial code above ready to run.