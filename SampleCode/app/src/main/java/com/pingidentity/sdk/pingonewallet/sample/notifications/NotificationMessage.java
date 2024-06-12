package com.pingidentity.sdk.pingonewallet.sample.notifications;

class NotificationMessage {

    private Alert alert;

    public NotificationMessage() {
    }

    public NotificationMessage(Alert alert) {
        this.alert = alert;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    static public class Alert {
        private String title;
        private String body;

        public Alert() {
        }

        public Alert(String title, String body) {
            this.title = title;
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
