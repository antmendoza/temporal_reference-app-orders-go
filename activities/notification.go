package activities

import (
	"context"
	"fmt"
	"time"

	"github.com/temporalio/orders-reference-app-go/pkg/ordersapi"
	mail "github.com/xhit/go-simple-mail/v2"
)

const from = "orders@reference-app.example"
const to = "customer@reference-app.example"

type ShipmentCreatedNotificationInput struct {
	OrderID ordersapi.OrderID
}

func (a *Activities) ShipmentCreatedNotification(ctx context.Context, input ShipmentCreatedNotificationInput) error {
	err := a.sendMail(from, to,
		fmt.Sprintf("Shipment for order: %s", string(input.OrderID)),
		"Your order has been processed and shipping has been arranged with the courier. We'll be in touch once its dispatched.",
	)

	return err
}

type ShipmentDispatchedNotificationInput struct {
	OrderID ordersapi.OrderID
}

func (a *Activities) ShipmentDispatchedNotification(ctx context.Context, input ShipmentDispatchedNotificationInput) error {
	err := a.sendMail(from, to,
		fmt.Sprintf("Shipment dispatched for order: %s", string(input.OrderID)),
		"Your order has been dispatched.",
	)

	return err
}

type ShipmentDeliveredNotificationInput struct {
	OrderID ordersapi.OrderID
}

func (a *Activities) ShipmentDeliveredNotification(ctx context.Context, input ShipmentDeliveredNotificationInput) error {
	err := a.sendMail(from, to,
		fmt.Sprintf("Shipment delivered for order: %s", string(input.OrderID)),
		"Your order has been delivered.",
	)

	return err
}

func (a *Activities) sendMail(from string, to string, subject string, body string) error {
	email := mail.NewMSG()
	email.SetFrom(from).
		AddTo(to).
		SetSubject(subject).
		SetBody(mail.TextPlain, body)

	if email.Error != nil {
		return email.Error
	}

	if a.SMTPStub {
		return nil
	}

	server := mail.NewSMTPClient()
	server.Host = a.SMTPHost
	server.Port = a.SMTPPort
	server.ConnectTimeout = time.Second
	server.SendTimeout = time.Second

	client, err := server.Connect()
	if err != nil {
		return err
	}

	return email.Send(client)
}
