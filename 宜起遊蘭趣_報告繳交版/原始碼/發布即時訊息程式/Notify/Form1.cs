using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace WindowsFormsApplication2
{
    public partial class Form1 : Form
    {
        String url;
        public Form1()
        {
            InitializeComponent();
            url = "140.125.45.113/gcm_server_php/";
            webBrowser1.Navigate(url);
            //webBrowser1.ObjectForScripting = this;
        }

        private void webBrowser1_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {

        }
    }
}
