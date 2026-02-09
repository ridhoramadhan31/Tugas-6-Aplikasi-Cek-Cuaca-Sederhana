/*
 * Aplikasi Cek Cuaca Sederhana
 * Mengambil data cuaca real-time dari OpenWeatherMap API
 * Menyimpan lokasi favorit dan data cuaca ke CSV
 */

 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import org.json.JSONObject;
 
 public class FormCekCuacaSederhana extends JFrame {
     private JTextField txtLokasi;
     private JComboBox<String> comboFavorit;
     private JLabel lblSuhu, lblKondisi, lblKelembapan, lblGambarCuaca;
     private JTable tblData;
     private DefaultTableModel model;
     private java.util.List<String> daftarFavorit = new ArrayList<>();
 
     // Ganti API_KEY ini dengan milik Anda dari https://openweathermap.org/api
     private static final String API_KEY = "ISI_API_KEY_ANDA_DI_SINI";
     private static final String FILE_FAVORIT = "favorit.txt";
     private static final String FILE_DATA = "data_cuaca.csv";
 
     public FormCekCuacaSederhana() {
         setTitle("Aplikasi Cek Cuaca Sederhana");
         setSize(600, 500);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
         setLayout(new BorderLayout(10, 10));
 
         // Panel Kontrol
         JPanel panelKontrol = new JPanel(new FlowLayout());
         panelKontrol.add(new JLabel("Lokasi:"));
         txtLokasi = new JTextField(10);
         panelKontrol.add(txtLokasi);
 
         comboFavorit = new JComboBox<>();
         comboFavorit.addItem("-- Pilih Favorit --");
         panelKontrol.add(comboFavorit);
 
         JButton btnCek = new JButton("Cek Cuaca");
         JButton btnSimpanFav = new JButton("Simpan Favorit");
         panelKontrol.add(btnCek);
         panelKontrol.add(btnSimpanFav);
 
         add(panelKontrol, BorderLayout.NORTH);
 
         // Panel Hasil Cuaca
         JPanel panelHasil = new JPanel(new GridLayout(4, 1));
         lblSuhu = new JLabel("Suhu: -", SwingConstants.CENTER);
         lblKondisi = new JLabel("Kondisi: -", SwingConstants.CENTER);
         lblKelembapan = new JLabel("Kelembapan: -", SwingConstants.CENTER);
         lblGambarCuaca = new JLabel("", SwingConstants.CENTER);
         panelHasil.add(lblSuhu);
         panelHasil.add(lblKondisi);
         panelHasil.add(lblKelembapan);
         panelHasil.add(lblGambarCuaca);
 
         add(panelHasil, BorderLayout.CENTER);
 
         // Panel Tabel Data
         JPanel panelData = new JPanel(new BorderLayout());
         model = new DefaultTableModel(new String[]{"Kota", "Suhu (°C)", "Kondisi", "Kelembapan (%)"}, 0);
         tblData = new JTable(model);
         panelData.add(new JScrollPane(tblData), BorderLayout.CENTER);
 
         JPanel panelButtonData = new JPanel(new FlowLayout());
         JButton btnSimpanCSV = new JButton("Simpan ke CSV");
         JButton btnMuatData = new JButton("Muat Data");
         panelButtonData.add(btnSimpanCSV);
         panelButtonData.add(btnMuatData);
         panelData.add(panelButtonData, BorderLayout.SOUTH);
 
         add(panelData, BorderLayout.SOUTH);
 
         // Muat data favorit
         muatFavorit();
 
         // --- EVENT LISTENER ---
         btnCek.addActionListener(e -> cekCuaca());
         btnSimpanFav.addActionListener(e -> simpanFavorit());
         btnSimpanCSV.addActionListener(e -> simpanDataCSV());
         btnMuatData.addActionListener(e -> muatDataCSV());
 
         comboFavorit.addItemListener(e -> {
             if (e.getStateChange() == ItemEvent.SELECTED) {
                 String kota = (String) comboFavorit.getSelectedItem();
                 if (kota != null && !kota.equals("-- Pilih Favorit --")) {
                     txtLokasi.setText(kota);
                     cekCuaca();
                 }
             }
         });
     }
 
     // --- Ambil data cuaca dari API ---
     private void cekCuaca() {
         String kota = txtLokasi.getText().trim();
         if (kota.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Masukkan nama kota terlebih dahulu!");
             return;
         }
 
         try {
             String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" +
                     URLEncoder.encode(kota, "UTF-8") +
                     "&appid=" + API_KEY + "&units=metric&lang=id";
 
             URL url = new URL(urlStr);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
 
             if (conn.getResponseCode() != 200) {
                 throw new IOException("Respon API tidak valid (kode: " + conn.getResponseCode() + ")");
             }
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuilder response = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null) response.append(line);
             reader.close();
 
             JSONObject json = new JSONObject(response.toString());
             double suhu = json.getJSONObject("main").getDouble("temp");
             int kelembapan = json.getJSONObject("main").getInt("humidity");
             String kondisi = json.getJSONArray("weather").getJSONObject(0).getString("description");
 
             // Tampilkan hasil
             lblSuhu.setText("Suhu: " + suhu + " °C");
             lblKelembapan.setText("Kelembapan: " + kelembapan + " %");
             lblKondisi.setText("Kondisi: " + kondisi);
             setGambarCuaca(kondisi);
 
             // Tambahkan ke tabel
             model.addRow(new Object[]{kota, suhu, kondisi, kelembapan});
 
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "Gagal mengambil data cuaca!\n" + ex.getMessage());
         }
     }
 
     // --- Ganti gambar sesuai kondisi ---
     private void setGambarCuaca(String kondisi) {
         String path = "";
         if (kondisi.toLowerCase().contains("cerah")) path = "sunny.png";
         else if (kondisi.toLowerCase().contains("awan")) path = "cloudy.png";
         else if (kondisi.toLowerCase().contains("hujan")) path = "rain.png";
         else path = "default.png";
 
         File f = new File(path);
         if (f.exists()) {
             lblGambarCuaca.setText("");
             lblGambarCuaca.setIcon(new ImageIcon(path));
         } else {
             lblGambarCuaca.setIcon(null);
             lblGambarCuaca.setText("(ikon tidak ditemukan)");
         }
     }
 
     // --- Simpan kota favorit ---
     private void simpanFavorit() {
         String kota = txtLokasi.getText().trim();
         if (kota.isEmpty()) return;
 
         if (!daftarFavorit.contains(kota)) {
             daftarFavorit.add(kota);
             comboFavorit.addItem(kota);
             try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_FAVORIT, true))) {
                 bw.write(kota);
                 bw.newLine();
                 JOptionPane.showMessageDialog(this, "Kota ditambahkan ke favorit!");
             } catch (IOException e) {
                 JOptionPane.showMessageDialog(this, "Gagal menyimpan favorit!");
             }
         }
     }
 
     // --- Muat daftar favorit ---
     private void muatFavorit() {
         File file = new File(FILE_FAVORIT);
         if (!file.exists()) return;
         try (BufferedReader br = new BufferedReader(new FileReader(file))) {
             String line;
             while ((line = br.readLine()) != null) {
                 daftarFavorit.add(line);
                 comboFavorit.addItem(line);
             }
         } catch (IOException ignored) {}
     }
 
     // --- Simpan data ke CSV ---
     private void simpanDataCSV() {
         try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_DATA))) {
             for (int i = 0; i < model.getRowCount(); i++) {
                 bw.write(model.getValueAt(i, 0) + "," +
                         model.getValueAt(i, 1) + "," +
                         model.getValueAt(i, 2) + "," +
                         model.getValueAt(i, 3));
                 bw.newLine();
             }
             JOptionPane.showMessageDialog(this, "Data berhasil disimpan ke CSV!");
         } catch (IOException e) {
             JOptionPane.showMessageDialog(this, "Gagal menyimpan data ke CSV!");
         }
     }
 
     // --- Muat data dari CSV ---
     private void muatDataCSV() {
         File file = new File(FILE_DATA);
         if (!file.exists()) {
             JOptionPane.showMessageDialog(this, "File data belum ada!");
             return;
         }
         try (BufferedReader br = new BufferedReader(new FileReader(file))) {
             model.setRowCount(0);
             String line;
             while ((line = br.readLine()) != null) {
                 String[] data = line.split(",");
                 model.addRow(data);
             }
             JOptionPane.showMessageDialog(this, "Data berhasil dimuat!");
         } catch (IOException e) {
             JOptionPane.showMessageDialog(this, "Gagal memuat data CSV!");
         }
     }
 
     // --- Main ---
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new FormCekCuacaSederhana().setVisible(true));
     }
 }
 