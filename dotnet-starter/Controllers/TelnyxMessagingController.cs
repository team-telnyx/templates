using System;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;
using System.IO;
using Newtonsoft.Json;

using Telnyx;
using Telnyx.net.Entities;

namespace dotnet_starter.Controllers
{
  [ApiController]
  [Route("messaging/[controller]")]
  public class OutboundController : ControllerBase
  {
    // POST messaging/Inbound
    [HttpPost]
    [Consumes("application/json")]
    public async Task<string> MessageDLRCallback([FromBody]TelnyxWebhook<OutboundMessage> webhook)
    {
      Console.WriteLine($"Received message with ID: {webhook.Data.Id}");
      return "";
    }
  }

  [ApiController]
  [Route("messaging/[controller]")]
  public class InboundController : ControllerBase
  {
    // POST messaging/Inbound
    [HttpPost]
    [Consumes("application/json")]
    public async Task<string> MessageInboundCallback()
    {
      string json;
      using (var reader = new StreamReader(Request.Body))
      {
        json = await reader.ReadToEndAsync();
      }
      dynamic webhook = JsonConvert.DeserializeObject<dynamic>(json);
      UriBuilder uriBuilder = new UriBuilder();
      uriBuilder.Scheme = Request.Scheme;
      uriBuilder.Host = Request.Host.ToString();
      uriBuilder.Path = "messaging/outbound";
      string dlrUri = uriBuilder.ToString();
      string From = webhook.data.payload.to[0].phone_number;
      string to = webhook.data.payload.from.phone_number;


      string TELNYX_API_KEY = System.Environment.GetEnvironmentVariable("TELNYX_API_KEY");
      TelnyxConfiguration.SetApiKey(TELNYX_API_KEY);
      MessagingSenderIdService service = new MessagingSenderIdService();
      NewMessagingSenderId options = new NewMessagingSenderId
      {
        From = From,
        To = to,
        Text = "Hello, World!",
        WebhookUrl = dlrUri,
        UseProfileWebhooks = false
      };
      MessagingSenderId messageResponse = await service.CreateAsync(options);
      Console.WriteLine($"Sent message with ID: {messageResponse.Id}");
      return "";
    }
  }
}