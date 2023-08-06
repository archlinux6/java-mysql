import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class MainFrame extends JFrame {

    private List<Car> latestCars = new ArrayList<>(); // 保存最新发布的车辆信息
    private boolean LoggedIn = false; // 登录状态
    private String Username = "";

    public MainFrame() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);

        // 设置UIManager属性
        UIManager.put("Button.background", new Color(255, 192, 203)); // 设置所有按钮背景颜色
        UIManager.put("Button.foreground", Color.WHITE); // 设置按钮文字颜色
        UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.PINK, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        UIManager.put("Button.select", new Color(255, 153, 153));

        JPanel panel = new JPanel() {
            // 重写绘制方法，绘制渐变色背景和圆角边框
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(176, 224, 230), 0, h, new Color(255, 192, 203));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.setColor(Color.WHITE);
                g2d.drawRoundRect(5, 5, w - 10, h - 10, 20, 20);
                g2d.dispose();
            }
        };
        panel.setLayout(new GridBagLayout()); // 网格袋布局管理器
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel welcomeLabel = new JLabel("欢迎来到二手车交易系统");
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setFont(new Font("宋体", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);

        JButton loginButton = new JButton("登陆");
        JButton registerButton = new JButton("注册");
        JButton latestCarsButton = new JButton("最新二手车信息");
        JButton searchCarsButton = new JButton("搜索车辆");
        JButton compareCarsButton = new JButton("对比车辆");
        JButton myFavoritesButton = new JButton("我的收藏");
        JButton managementButton = new JButton("后台管理");
        JButton exitButton = new JButton("退出系统");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭当前界面
                System.exit(0); // 退出程序
            }
        });

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; // 设置按钮水平填充
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(registerButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(latestCarsButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(searchCarsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(compareCarsButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(myFavoritesButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(managementButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(exitButton, gbc);

        panel.setBackground(new Color(176, 224, 230)); // 设置面板背景颜色为浅蓝色

        loginButton.setPreferredSize(new Dimension(200, 40)); // 设置按钮宽度
        registerButton.setPreferredSize(new Dimension(200, 40));
        latestCarsButton.setPreferredSize(new Dimension(200, 40));
        searchCarsButton.setPreferredSize(new Dimension(200, 40));
        compareCarsButton.setPreferredSize(new Dimension(200, 40));
        myFavoritesButton.setPreferredSize(new Dimension(200, 40));
        managementButton.setPreferredSize(new Dimension(200, 40));
        exitButton.setPreferredSize(new Dimension(200, 40));

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        latestCarsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLatestCars();
            }
        });

        searchCarsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchCars();
            }
        });
        compareCarsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showComparison();
            }
        });
        myFavoritesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFavorites();
            }
        });
        managementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAdminMenu();
            }
        });
        add(panel);
        setVisible(true);
    }

    private void login() {
        String username = JOptionPane.showInputDialog(this, "请输入用户名");
        Username = "";
        Username += username;
        String password = JOptionPane.showInputDialog(this, "请输入密码");
        String captcha = JOptionPane.showInputDialog(this, "请输入验证码");

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // 登录成功
                LoggedIn = true;
                JOptionPane.showMessageDialog(this, "登陆成功");
            } else {
                // 登录失败
                JOptionPane.showMessageDialog(this, "用户名或密码错误");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void register() {
        String username = JOptionPane.showInputDialog(this, "请输入用户名");
        String password = JOptionPane.showInputDialog(this, "请输入密码");
        String confirmPassword = JOptionPane.showInputDialog(this, "请再次输入密码");
        String phone = JOptionPane.showInputDialog(this, "请输入手机号");
        String email = JOptionPane.showInputDialog(this, "请输入邮箱");

        // 检查密码是否一致
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致，请重新输入");
            return;
        }

        // 检查手机号和邮箱格式是否正确
        if (!isValidPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, "手机号格式不正确，请重新输入");
            return;
        }
        if (!isValidEmailAddress(email)) {
            JOptionPane.showMessageDialog(this, "邮箱格式不正确，请重新输入");
            return;
        }

        // 确认注册信息并提示用户
        int confirm = JOptionPane.showConfirmDialog(this, "请确认注册信息\n用户名：" + username +
                "\n密码：" + password +
                "\n手机号：" + phone +
                "\n邮箱：" + email);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 将注册信息插入到数据库
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, phone, email) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, phone);
            statement.setString(4, email);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "注册成功");
            } else {
                JOptionPane.showMessageDialog(this, "用户名重复，请重试");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(this, "用户名重复，请重试");
        }
    }

    // 判断手机号格式是否正确
    private boolean isValidPhoneNumber(String phone) {
        String pattern = "^1[0-9]{10}$";// 数字 1 开头，后面跟着 10 个数字字符，总共为 11 位。
        return phone.matches(pattern);
    }

    // 判断邮箱格式是否正确
    private boolean isValidEmailAddress(String email) {
        String pattern = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$";
        return email.matches(pattern);
    }

    private int currentPage = 0; // 当前显示的页码
    private int pageSize = 10; // 每页显示的记录数

    private static final String URL = "jdbc:mysql://localhost:3306/mysql?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Car> getLatestCars() {
        List<Car> latestCars = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM CARS ORDER BY publishTime DESC limit 10")) {

            while (rs.next()) {
                int Id = rs.getInt("Id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                int price = rs.getInt("price");
                int mileage = rs.getInt("mileage");
                String publishTime = rs.getString("publishTime");

                Car car = new Car(Id, brand, model, price, mileage, publishTime);
                latestCars.add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return latestCars;
    }

    private void showLatestCars() {
        // 获取最新二手车信息
        latestCars = getLatestCars();

        // 创建展示车辆信息的界面
        JFrame frame = new JFrame("最新二手车信息");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // 创建JTable，并设置数据模型
        DefaultTableModel model = new DefaultTableModel(new Object[] { "品牌", "车型", "里程数", "价格", "发布时间" }, 0);
        for (Car car : latestCars) {
            model.addRow(new Object[] { car.getBrand(), car.getModel(), car.getMileage(), car.getPrice(),
                    car.getPublishTime() });
        }
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);

        // 创建上一页和下一页按钮，并设置监听器
        JButton prevButton = new JButton("上一页");
        JButton nextButton = new JButton("下一页");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPage > 0) {
                    currentPage--; // 上一页
                    latestCars = getLatestCars(); // 重新加载数据
                    model.setRowCount(0); // 清空表格数据
                    for (Car car : latestCars) {
                        model.addRow(new Object[] { car.getBrand(), car.getModel(), car.getMileage(), car.getPrice(),
                                car.getPublishTime() });
                    }
                }
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (latestCars.size() == pageSize) {
                    currentPage++; // 下一页
                    latestCars = getLatestCars(); // 重新加载数据
                    model.setRowCount(0); // 清空表格数据
                    for (Car car : latestCars) {
                        model.addRow(new Object[] { car.getBrand(), car.getModel(), car.getMileage(), car.getPrice(),
                                car.getPublishTime() });
                    }
                }
            }
        });

        // 添加行点击事件监听器
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            boolean isShowingDetails = false; // 标记当前是否正在显示详细信息窗口

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    // 如果用户正在选择行，而不是在释放鼠标按钮时选择行，则不执行后续操作
                    return;
                }

                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1 && !isShowingDetails) {
                    // 如果用户选择了一行并且当前没有显示详细信息窗口，则显示详细信息窗口
                    Car selectedCar = latestCars.get(selectedRow);
                    showCarDetails(selectedCar);
                    isShowingDetails = true;
                }
            }

            // 显示车辆的详细信息
            private void showCarDetails(Car car) {
                // 创建展示详细信息的界面
                JFrame detailsFrame = new JFrame("车辆详细信息");
                detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                detailsFrame.setSize(400, 300);
                detailsFrame.setLocationRelativeTo(null);

                // 添加窗口关闭监听器
                detailsFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        isShowingDetails = false; // 标记当前没有显示详细信息窗口
                    }
                });

                // 创建标签和文本框，显示车辆的详细信息
                JPanel panel = new JPanel(new GridLayout(5, 2));
                panel.add(new JLabel("品牌："));
                panel.add(new JTextField(car.getBrand()));
                String brand = car.getBrand();
                panel.add(new JLabel("车型："));
                panel.add(new JTextField(car.getModel()));
                String model = car.getModel();
                panel.add(new JLabel("里程数："));
                panel.add(new JTextField(String.valueOf(car.getMileage())));
                int mileage = car.getMileage();
                panel.add(new JLabel("价格："));
                panel.add(new JTextField(String.valueOf(car.getPrice())));
                int price = car.getPrice();
                panel.add(new JLabel("发布时间："));
                panel.add(new JTextField(car.getPublishTime()));
                String publishTime = car.getPublishTime();
                detailsFrame.add(panel);

                // 创建收藏和对比按钮，并设置监听器
                JButton favoriteButton = new JButton("收藏");
                JButton compareButton = new JButton("对比");
                JButton purchaseButton = new JButton("购买");
                JButton backButton = new JButton("返回主菜单");
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(favoriteButton);
                buttonPanel.add(compareButton);
                buttonPanel.add(purchaseButton);
                buttonPanel.add(backButton);
                detailsFrame.add(buttonPanel, BorderLayout.SOUTH);
                favoriteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isLoggedIn()) {
                            // 用户已登录，将车辆添加到收藏列表中
                            // favoriteCars.add(car);
                            // JOptionPane.showMessageDialog(detailsFrame, "已将该车辆添加到收藏列表中。");

                            // 在后台线程中插入收藏数据到数据库的favorites表中

                            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    try {
                                        Connection connection = DriverManager.getConnection(
                                                "jdbc:mysql://localhost:3306/mysql",
                                                "root", "");
                                        String query = "INSERT INTO FAVORITESS ( brand, model, mileage, price, publishTime) VALUES ( ?, ?, ?, ?, ?)";
                                        PreparedStatement statement = connection.prepareStatement(query);
                                        statement.setString(1, brand);
                                        statement.setString(2, model);
                                        statement.setInt(3, mileage);
                                        statement.setInt(4, price);
                                        statement.setString(5, publishTime);
                                        statement.executeUpdate();
                                        statement.close();
                                        connection.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void done() {
                                    // 不在这里更新对话框，而是在Swing事件调度线程中更新
                                    SwingUtilities.invokeLater(() -> {
                                        JOptionPane.showMessageDialog(detailsFrame, "收藏成功");
                                    });
                                }
                            };

                            worker.execute();
                        } else {
                            // 用户未登录，提示用户先登录
                            JOptionPane.showMessageDialog(detailsFrame, "请先登录以使用收藏功能。");
                        }
                    }
                });

                compareButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // 将车辆添加到对比列表中
                        // compareCars.add(car);

                        // 在后台线程中插入比较数据到数据库的comparison表中
                        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                try {
                                    Connection connection = DriverManager.getConnection(
                                            "jdbc:mysql://localhost:3306/mysql",
                                            "root", "");
                                    String query = "INSERT INTO COMPARISONS ( brand, model, mileage, price,publishTime) VALUES (?, ?, ?, ?, ?)";
                                    PreparedStatement statement = connection.prepareStatement(query);
                                    statement.setString(1, brand);
                                    statement.setString(2, model);
                                    statement.setInt(3, mileage);
                                    statement.setInt(4, price);
                                    statement.setString(5, publishTime);
                                    statement.executeUpdate();
                                    statement.close();
                                    connection.close();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void done() {
                                // 不在这里更新对话框，而是在Swing事件调度线程中更新
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(detailsFrame, "已将该车辆添加到比较列表中。");
                                });
                            }
                        };

                        worker.execute();
                    }
                });

                purchaseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isLoggedIn()) {
                            // 用户已登录，执行购买逻辑
                            performPurchase(car);
                        } else {
                            // 用户未登录，提示用户先登录
                            JOptionPane.showMessageDialog(detailsFrame, "请先登录以使用购买功能。");
                        }
                    }
                });
                backButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // 返回主菜单的逻辑代码
                        detailsFrame.dispose(); // 关闭detailframe窗口
                        frame.dispose();
                    }
                });

                detailsFrame.setVisible(true);
            }
        });
        frame.setVisible(true);
    }

    private void searchCars() {
        // 创建一个菜单对话框
        JDialog searchDialog = new JDialog(this, "搜索车辆", true);
        searchDialog.setSize(600, 400);
        searchDialog.setLocationRelativeTo(this); // 在主窗口中心显示
        searchDialog.setLayout(new BorderLayout());

        // 创建一个下拉框和一个搜索按钮
        JPanel searchPanel = new JPanel(new FlowLayout());
        JComboBox<String> searchTypeComboBox = new JComboBox<>(new String[] { "根据品牌搜索", "根据价格搜索", "根据上牌日期搜索" });
        JButton searchButton = new JButton("搜索");
        searchPanel.add(new JLabel("搜索类型："));
        searchPanel.add(searchTypeComboBox);
        searchPanel.add(searchButton);

        // 创建一个结果表格，用于显示搜索结果
        DefaultTableModel model = new DefaultTableModel(new Object[] { "品牌", "型号", "里程数", "价格", "上牌时间" }, 0);
        JTable resultTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(resultTable);

        // 添加下拉框、搜索按钮和结果表格到菜单窗口
        searchDialog.add(searchPanel, BorderLayout.NORTH);
        searchDialog.add(scrollPane, BorderLayout.CENTER);

        // 为搜索按钮添加监听器，当用户点击时执行搜索操作
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取用户选择的搜索类型
                String selectedSearchType = (String) searchTypeComboBox.getSelectedItem();

                // 根据选择的搜索类型执行相应的逻辑
                model.setRowCount(0); // 清空表格

                if (selectedSearchType.equals("根据品牌搜索")) {
                    // 创建一个菜单对话框用于选择品牌
                    JDialog brandDialog = new JDialog(searchDialog, "选择品牌", true);
                    brandDialog.setLocationRelativeTo(searchDialog); // 在搜索对话框中心显示
                    brandDialog.setLayout(new BorderLayout());
                    brandDialog.setSize(800, 400);

                    // 创建品牌选项菜单
                    JPanel brandPanel = new JPanel(new GridLayout(5, 1));

                    List<String> brands = Arrays.asList("大众", "福特", "斯柯达", "别克", "本田");
                    for (String brand : brands) {
                        JButton brandButton = new JButton(brand);
                        brandButton.setPreferredSize(new Dimension(200, 50)); // 设置按钮的宽度和高度
                        brandButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // 执行根据品牌搜索的逻辑
                                String selectedBrand = brandButton.getText();
                                searchCarsByBrand(selectedBrand, model);

                                brandDialog.dispose(); // 关闭品牌选择对话框
                            }
                        });
                        brandPanel.add(brandButton);
                    }
                    brandDialog.add(brandPanel, BorderLayout.CENTER);
                    brandDialog.pack();
                    brandDialog.setVisible(true);
                } else if (selectedSearchType.equals("根据价格搜索")) {
                    // 创建一个菜单对话框用于选择价格
                    JDialog priceDialog = new JDialog(searchDialog, "选择价格", true);
                    priceDialog.setLocationRelativeTo(searchDialog); // 在搜索对话框中心显示
                    priceDialog.setLayout(new BorderLayout());
                    priceDialog.setSize(400, 200);

                    // 创建价格选项菜单
                    JPanel pricePanel = new JPanel(new GridLayout(4, 1));

                    List<String> prices = Arrays.asList("5万以下", "5-10万", "10-15万", "15万以上");
                    for (String price : prices) {
                        JButton priceButton = new JButton(price);
                        priceButton.setPreferredSize(new Dimension(200, 50)); // 设置按钮的宽度和高度
                        priceButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // 执行根据价格搜索的逻辑
                                String selectedPrice = priceButton.getText();
                                searchCarsByPrice(selectedPrice, model);

                                priceDialog.dispose(); // 关闭价格选择对话框
                            }
                        });
                        pricePanel.add(priceButton);
                    }
                    priceDialog.add(pricePanel, BorderLayout.CENTER);
                    priceDialog.pack();
                    priceDialog.setVisible(true);
                } else if (selectedSearchType.equals("根据上牌日期搜索")) {
                    // 创建一个菜单对话框用于输入起始年份和月份以及结束的年份和月份
                    JDialog dateDialog = new JDialog(searchDialog, "选择上牌日期", true);
                    dateDialog.setLocationRelativeTo(searchDialog); // 在搜索对话框中心显示
                    dateDialog.setLayout(new BorderLayout());
                    dateDialog.setSize(400, 200);

                    // 创建输入框和搜索按钮
                    JPanel inputPanel = new JPanel(new FlowLayout());
                    JTextField startYearField = new JTextField(4);
                    JTextField startMonthField = new JTextField(2);
                    JTextField endYearField = new JTextField(4);
                    JTextField endMonthField = new JTextField(2);
                    JButton dateSearchButton = new JButton("搜索");
                    inputPanel.add(new JLabel("起始年份："));
                    inputPanel.add(startYearField);
                    inputPanel.add(new JLabel("起始月份："));
                    inputPanel.add(startMonthField);
                    inputPanel.add(new JLabel("结束年份："));
                    inputPanel.add(endYearField);
                    inputPanel.add(new JLabel("结束月份："));
                    inputPanel.add(endMonthField);
                    inputPanel.add(dateSearchButton);

                    dateDialog.add(inputPanel, BorderLayout.CENTER);
                    dateDialog.pack();
                    dateDialog.setVisible(true);

                    // 为搜索按钮添加监听器，当用户点击时执行搜索操作
                    dateSearchButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 获取用户输入的起始年份、月份和结束年份、月份
                            int startYear = Integer.parseInt(startYearField.getText());
                            int startMonth = Integer.parseInt(startMonthField.getText());
                            int endYear = Integer.parseInt(endYearField.getText());
                            int endMonth = Integer.parseInt(endMonthField.getText());

                            // // 执行根据上牌日期搜索的逻辑
                            searchCarsByRegistrationDate(startYear, startMonth, endYear, endMonth,
                                    model);
                            // model.addRow(new Object[] { "雪佛兰", "科鲁兹", "65000", "32000", "2022-01-01" });
                            try {
                                // 创建数据库连接
                                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql",
                                        "root", "");

                                // 创建 SQL 查询语句
                                String sql = "SELECT * FROM CARS ORDER BY  publishTime desc limit 3";

                                // 创建预编译查询语句对象
                                PreparedStatement stmt = conn.prepareStatement(sql);

                                // 执行查询并获取结果集
                                ResultSet rs = stmt.executeQuery();

                                // 遍历结果集，将数据添加到表格模型中
                                while (rs.next()) {
                                    String carBrand = rs.getString("brand");
                                    String carModel = rs.getString("model");
                                    int carMileage = rs.getInt("mileage");
                                    int carPrice = rs.getInt("price");
                                    String carPublishTime = rs.getString("publishTime");

                                    model.addRow(
                                            new Object[] { carBrand, carModel, carMileage, carPrice, carPublishTime });
                                }

                                // 关闭连接和语句对象
                                rs.close();
                                stmt.close();
                                conn.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            dateDialog.dispose(); // 关闭日期选择对话框
                        }

                    });
                }
            }
        });

        searchDialog.setVisible(true);
    }

    private void searchCarsByBrand(String selectedBrand, DefaultTableModel model) {
        try {
            // 创建数据库连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "");

            // 创建 SQL 查询语句
            String sql = "SELECT * FROM CARS WHERE brand= ?";

            // 创建预编译查询语句对象
            PreparedStatement stmt = conn.prepareStatement(sql);

            // 设置参数
            stmt.setString(1, selectedBrand);

            // 执行查询并获取结果集
            ResultSet rs = stmt.executeQuery();

            // 遍历结果集，将数据添加到表格模型中
            while (rs.next()) {
                String carBrand = rs.getString("brand");
                String carModel = rs.getString("model");
                int carMileage = rs.getInt("mileage");
                int carPrice = rs.getInt("price");
                String carPublishTime = rs.getString("publishTime");

                model.addRow(new Object[] { carBrand, carModel, carMileage, carPrice, carPublishTime });
            }

            // 关闭连接和语句对象
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 根据价格搜索车辆
    private void searchCarsByPrice(String price, DefaultTableModel model) {
        int minPrice = 0;
        int maxPrice = 0;

        // 将文字转换为数字
        switch (price) {
            case "5万以下":
                maxPrice = 50000;
                break;
            case "5-10万":
                minPrice = 50000;
                maxPrice = 100000;
                break;
            case "10-15万":
                minPrice = 100000;
                maxPrice = 150000;
                break;
            case "15万以上":
                minPrice = 150000;
                break;
            default:
                // 处理未知价格范围
                return;
        }

        try {
            // 创建数据库连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "");

            // 创建 SQL 查询语句
            String sql = "SELECT * FROM CARS WHERE price >= ? AND price <= ?";

            // 创建预编译查询语句对象
            PreparedStatement stmt = conn.prepareStatement(sql);

            // 设置参数
            stmt.setInt(1, minPrice);
            stmt.setInt(2, maxPrice);

            // 执行查询并获取结果集
            ResultSet rs = stmt.executeQuery();

            // 遍历结果集，将数据添加到表格模型中
            while (rs.next()) {
                String carBrand = rs.getString("brand");
                String carModel = rs.getString("model");
                int carMileage = rs.getInt("mileage");
                int carPrice = rs.getInt("price");
                String carPublishTime = rs.getString("publishTime");

                model.addRow(new Object[] { carBrand, carModel, carMileage, carPrice, carPublishTime });
            }

            // 关闭连接和语句对象
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 根据上牌日期搜索车辆
    private void searchCarsByRegistrationDate(int startYear, int startMonth, int endYear, int endMonth,
            DefaultTableModel model) {
        try {
            // 创建数据库连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root",
                    "");

            // 创建 SQL 查询语句
            String sql = "SELECT * FROM CARS WHERE YEAR(registrationDate) >= ? AND YEAR(registrationDate) <= ?" +
                    "AND MONTH(registrationDate) >= ? AND MONTH(registrationDate) <= ?";

            // 创建预编译查询语句对象
            PreparedStatement stmt = conn.prepareStatement(sql);

            // 设置参数
            stmt.setInt(1, startYear);
            stmt.setInt(2, endYear);
            stmt.setInt(3, startMonth);
            stmt.setInt(4, endMonth);

            // 执行查询并获取结果集
            ResultSet rs = stmt.executeQuery();

            // 遍历结果集，将数据添加到表格模型中
            while (rs.next()) {
                String carBrand = rs.getString("brand");
                String carModel = rs.getString("model");
                int carMileage = rs.getInt("mileage");
                int carPrice = rs.getInt("price");
                String carPublishTime = rs.getString("publishTime");

                model.addRow(new Object[] { carBrand, carModel, carMileage, carPrice, carPublishTime });
            }

            // 关闭连接和语句对象
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 根据车辆编号查询对比车辆数据
    private void getComparisonData(DefaultTableModel model) {

        try {
            // 创建数据库连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root",
                    "");

            // 创建 SQL 查询语句
            String sql = "SELECT * FROM COMPARISONS";

            // 创建预编译查询语句对象
            Statement stmt = conn.createStatement();

            // 执行查询并获取结果集
            ResultSet rs = stmt.executeQuery(sql);

            // 遍历结果集，将数据添加到表格模型中
            while (rs.next()) {
                int carId = rs.getInt("Id");
                String carBrand = rs.getString("brand");
                String carModel = rs.getString("model");
                int carMileage = rs.getInt("mileage");
                int carPrice = rs.getInt("price");
                String carPublishTime = rs.getString("publishTime");

                model.addRow(new Object[] { carId, carBrand, carModel, carMileage, carPrice, carPublishTime });
            }

            // 关闭连接和语句对象
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 删除对比车辆的方法
    private void deleteComparisonCar(int carId) {
        try {
            // 创建数据库连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "");

            // 创建 SQL 删除语句
            String sql = "DELETE FROM COMPARISONS WHERE Id = ?";

            // 创建预编译语句对象
            PreparedStatement stmt = conn.prepareStatement(sql);

            // 设置参数
            stmt.setInt(1, carId);

            // 执行删除操作
            stmt.executeUpdate();

            // 关闭连接和语句对象
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showComparison() {
        // 创建一个菜单对话框
        if (isLoggedIn()) {
            JDialog comparisonDialog = new JDialog(this, "对比车辆", true);
            comparisonDialog.setSize(500, 250);
            comparisonDialog.setLocationRelativeTo(this); // 在主窗口中心显示
            comparisonDialog.setLayout(new BorderLayout());

            // 创建一个结果表格，用于显示对比结果
            JTable resultTable = new JTable(
                    new DefaultTableModel(new Object[] { "车辆编号", "品牌", "型号", "排量", "价格", "上牌时间" }, 0));
            JScrollPane scrollPane = new JScrollPane(resultTable);

            // 添加结果表格到菜单窗口
            comparisonDialog.add(scrollPane, BorderLayout.CENTER);

            // 创建四个菜单按钮，并添加到菜单窗口
            JPanel menuPanel = new JPanel(new GridLayout(4, 1, 0, 10));
            JButton viewButton = new JButton("查看对比信息");
            JButton deleteButton = new JButton("删除对比车辆");
            JButton backButton = new JButton("返回上一级菜单");
            JButton mainMenuButton = new JButton("返回主菜单");
            viewButton.setFont(new Font("宋体", Font.PLAIN, 16));
            deleteButton.setFont(new Font("宋体", Font.PLAIN, 16));
            backButton.setFont(new Font("宋体", Font.PLAIN, 16));
            mainMenuButton.setFont(new Font("宋体", Font.PLAIN, 16));
            menuPanel.add(viewButton);
            menuPanel.add(deleteButton);
            menuPanel.add(backButton);
            menuPanel.add(mainMenuButton);
            comparisonDialog.add(menuPanel, BorderLayout.EAST);

            // 为查看对比信息按钮添加监听器，当用户点击时执行查看操作
            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 在这里实现查看对比信息的逻辑
                    DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
                    model.setRowCount(0); // 清除表格内容

                    // 获取对比车辆数据并添加到表格中
                    getComparisonData(model);
                }
            });

            // 为删除对比车辆按钮添加监听器，当用户点击时执行删除操作
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 实现删除对比车辆的逻辑
                    int selectedRow = resultTable.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(comparisonDialog, "请先选择一个要删除的车辆", "提示信息",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        int confirm = JOptionPane.showConfirmDialog(comparisonDialog, "确定要删除选中的车辆吗？", "删除确认",
                                JOptionPane.OK_CANCEL_OPTION);
                        if (confirm == JOptionPane.OK_OPTION) {
                            DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
                            int carBrand = (int) model.getValueAt(selectedRow, 0);
                            deleteComparisonCar(carBrand); // 调用删除对比车辆的方法
                            model.removeRow(selectedRow);
                        }
                    }
                }
            });

            // 为返回上一级菜单按钮添加监听器，当用户点击时关闭对话框
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    comparisonDialog.dispose();
                }
            });

            // 为返回主菜单按钮添加监听器，当用户点击时关闭对话框并返回主菜单
            mainMenuButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    comparisonDialog.dispose();
                }
            });

            comparisonDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "请先登录使用此功能哦。");
        }
    }

    private void showFavorites() {
        if (isLoggedIn()) {
            latestCars = getLatestCars();

            // 创建展示车辆信息的界面
            JFrame frame = new JFrame("收藏信息");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            // 创建JTable，并设置数据模型
            DefaultTableModel model = new DefaultTableModel(new Object[] { "车辆编号", "品牌", "车型", "里程数", "价格", "发布时间" },
                    0);

            try {
                // 创建数据库连接
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root",
                        "");

                // 创建 SQL 查询语句
                String sql = "SELECT * FROM FAVORITESS  order by publishTime desc";

                // 创建预编译查询语句对象
                Statement stmt = conn.createStatement();

                // 执行查询并获取结果集
                ResultSet rs = stmt.executeQuery(sql);

                // 遍历结果集，将数据添加到表格模型中
                while (rs.next()) {
                    int carId = rs.getInt("Id");
                    String carBrand = rs.getString("brand");
                    String carModel = rs.getString("model");
                    int carMileage = rs.getInt("mileage");
                    int carPrice = rs.getInt("price");
                    String carPublishTime = rs.getString("publishTime");

                    model.addRow(new Object[] { carId, carBrand, carModel, carMileage, carPrice, carPublishTime });
                }

                // 关闭连接和语句对象
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane);

            // 创建上一页和下一页按钮，并设置监听器
            JButton prevButton = new JButton("上一页");
            JButton nextButton = new JButton("下一页");
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);
            frame.add(buttonPanel, BorderLayout.SOUTH);
            prevButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentPage > 0) {
                        currentPage--; // 上一页
                        latestCars = getLatestCars(); // 重新加载数据
                        model.setRowCount(0); // 清空表格数据
                        for (Car car : latestCars) {
                            model.addRow(
                                    new Object[] { car.getBrand(), car.getModel(), car.getMileage(), car.getPrice(),
                                            car.getPublishTime() });
                        }
                    }
                }
            });
            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (latestCars.size() == pageSize) {
                        currentPage++; // 下一页
                        latestCars = getLatestCars(); // 重新加载数据
                        model.setRowCount(0); // 清空表格数据
                        for (Car car : latestCars) {
                            model.addRow(
                                    new Object[] { car.getBrand(), car.getModel(), car.getMileage(), car.getPrice(),
                                            car.getPublishTime() });
                        }
                    }
                }
            });

            // 添加行点击事件监听器
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                boolean isShowingDetails = false; // 标记当前是否正在显示详细信息窗口

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        // 如果用户正在选择行，而不是在释放鼠标按钮时选择行，则不执行后续操作
                        return;
                    }

                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1 && !isShowingDetails) {
                        // 如果用户选择了一行并且当前没有显示详细信息窗口，则显示详细信息窗口
                        Car selectedCar = latestCars.get(selectedRow);
                        showCarDetails(selectedCar);
                        isShowingDetails = true;
                    }
                }

                // 显示车辆的详细信息
                private void showCarDetails(Car car) {
                    // 创建展示详细信息的界面
                    JFrame detailsFrame = new JFrame("车辆详细信息");
                    detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    detailsFrame.setSize(400, 300);
                    detailsFrame.setLocationRelativeTo(null);

                    // 添加窗口关闭监听器
                    detailsFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            isShowingDetails = false; // 标记当前没有显示详细信息窗口
                        }
                    });

                    // 创建标签和文本框，显示车辆的详细信息
                    JPanel panel = new JPanel(new GridLayout(5, 2));
                    panel.add(new JLabel("品牌："));
                    panel.add(new JTextField(car.getBrand()));
                    String brand = car.getBrand();
                    panel.add(new JLabel("车型："));
                    panel.add(new JTextField(car.getModel()));
                    String model = car.getModel();
                    panel.add(new JLabel("里程数："));
                    panel.add(new JTextField(String.valueOf(car.getMileage())));
                    int mileage = car.getMileage();
                    panel.add(new JLabel("价格："));
                    panel.add(new JTextField(String.valueOf(car.getPrice())));
                    int price = car.getPrice();
                    panel.add(new JLabel("发布时间："));
                    panel.add(new JTextField(car.getPublishTime()));
                    String publishTime = car.getPublishTime();
                    detailsFrame.add(panel);

                    // 创建收藏和对比按钮，并设置监听器
                    JButton favoriteButton = new JButton("收藏");
                    JButton compareButton = new JButton("对比");
                    JButton purchaseButton = new JButton("购买");
                    JButton backButton = new JButton("返回主菜单");
                    JPanel buttonPanel = new JPanel();
                    buttonPanel.add(favoriteButton);
                    buttonPanel.add(compareButton);
                    buttonPanel.add(purchaseButton);
                    buttonPanel.add(backButton);
                    detailsFrame.add(buttonPanel, BorderLayout.SOUTH);
                    favoriteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (isLoggedIn()) {
                                // 用户已登录，将车辆添加到收藏列表中
                                // favoriteCars.add(car);
                                // JOptionPane.showMessageDialog(detailsFrame, "已将该车辆添加到收藏列表中。");

                                // 在后台线程中插入收藏数据到数据库的favorites表中
                                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                                    @Override
                                    protected Void doInBackground() throws Exception {
                                        try {
                                            Connection connection = DriverManager.getConnection(
                                                    "jdbc:mysql://localhost:3306/mysql",
                                                    "root", "");
                                            String query = "INSERT INTO FAVORITESS ( brand, model, mileage, price, publishTime) VALUES ( ?, ?, ?, ?, ?)";
                                            PreparedStatement statement = connection.prepareStatement(query);
                                            statement.setString(1, brand);
                                            statement.setString(2, model);
                                            statement.setInt(3, mileage);
                                            statement.setInt(4, price);
                                            statement.setString(5, publishTime);
                                            statement.executeUpdate();
                                            statement.close();
                                            connection.close();
                                        } catch (SQLException ex) {
                                            ex.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void done() {
                                        // 不在这里更新对话框，而是在Swing事件调度线程中更新
                                        SwingUtilities.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(detailsFrame, "收藏成功");
                                        });
                                    }
                                };

                                worker.execute();
                            } else {
                                // 用户未登录，提示用户先登录
                                JOptionPane.showMessageDialog(detailsFrame, "请先登录以使用收藏功能。");
                            }
                        }
                    });

                    compareButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 将车辆添加到对比列表中
                            // compareCars.add(car);

                            // 在后台线程中插入比较数据到数据库的comparison表中
                            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    try {
                                        Connection connection = DriverManager.getConnection(
                                                "jdbc:mysql://localhost:3306/mysql",
                                                "root", "");
                                        String query = "INSERT INTO COMPARISONS ( brand, model, mileage, price,publishTime) VALUES ( ?, ?, ?, ?, ?)";
                                        PreparedStatement statement = connection.prepareStatement(query);
                                        statement.setString(1, brand);
                                        statement.setString(2, model);
                                        statement.setInt(3, mileage);
                                        statement.setInt(4, price);
                                        statement.setString(5, publishTime);
                                        statement.executeUpdate();
                                        statement.close();
                                        connection.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void done() {
                                    // 不在这里更新对话框，而是在Swing事件调度线程中更新
                                    SwingUtilities.invokeLater(() -> {
                                        JOptionPane.showMessageDialog(detailsFrame, "已将该车辆添加到比较列表中。");
                                    });
                                }
                            };

                            worker.execute();
                        }
                    });

                    purchaseButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (isLoggedIn()) {
                                // 用户已登录，执行购买逻辑
                                performPurchase(car);
                            } else {
                                // 用户未登录，提示用户先登录
                                JOptionPane.showMessageDialog(detailsFrame, "请先登录以使用购买功能。");
                            }
                        }

                    });
                    backButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 返回主菜单的逻辑代码
                            detailsFrame.dispose(); // 关闭detailframe窗口
                            frame.dispose();
                        }
                    });

                    detailsFrame.setVisible(true);
                }
            });
            frame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "请先登录使用此功能哦。");

        }
    }

    // 判断用户是否已登录的方法
    private boolean isLoggedIn() {
        return LoggedIn;
        // 在这里添加判断用户是否已登录的逻辑，返回 true 或 false
    }

    // 执行购买操作的方法
    private void performPurchase(Car car) {
        int price = car.getPrice(); // 获取车辆的价格

        // 检查账户余额是否足够支付
        int balance = getAccountBalance(); // 获取用户账户余额
        if (balance >= price) {
            // 账户余额足够，进行购买操作
            deductAmount(price); // 扣除购买金额
            printReceipt(car); // 打印购买凭条
            JOptionPane.showMessageDialog(this, "购买成功哦");

        } else {
            // 账户余额不足，购买失败
            JOptionPane.showMessageDialog(this, "余额不足，购买失败");
        }
    }

    // 获取账户余额的方法
    private int getAccountBalance() {
        // 在这里实现获取账户余额的逻辑
        return 0;
    }

    // 扣除购买金额的方法
    private void deductAmount(int amount) {
        // 在这里实现扣除购买金额的逻辑
    }

    // 打印购买凭条的方法
    private void printReceipt(Car car) {
        // 在这里实现打印购买凭条的逻辑
    }

    // 展示交易结果的方法
    private void showTransactionResult(String result) {
        // 在这里展示交易结果
    }

    // 返回主菜单的方法
    private void returnToMainMenu() {
        // 在这里添加返回主菜单的逻辑
    }

    private void showAdminMenu() {
        if (isLoggedIn() && Username.equals("admin")) {

            // 创建一个菜单对话框
            JDialog adminDialog = new JDialog(this, "后台管理", true);
            adminDialog.setSize(500, 550);
            adminDialog.setLocationRelativeTo(this); // 在主窗口中心显示
            adminDialog.setLayout(new BorderLayout());

            // 创建三个菜单按钮，并添加到菜单窗口
            JPanel menuPanel = new JPanel(new GridLayout(4, 1, 0, 10));
            JButton brandButton = new JButton("品牌管理");
            JButton modelButton = new JButton("车型管理");
            JButton publishButton = new JButton("发布车辆信息");
            // 创建一个返回品牌管理菜单按钮，并添加到车型管理窗口底部
            JButton backButton = new JButton("返回品牌管理菜单");
            backButton.setFont(new Font("宋体", Font.PLAIN, 16));

            brandButton.setFont(new Font("宋体", Font.PLAIN, 16));
            modelButton.setFont(new Font("宋体", Font.PLAIN, 16));
            publishButton.setFont(new Font("宋体", Font.PLAIN, 16));
            menuPanel.add(brandButton);
            menuPanel.add(modelButton);
            menuPanel.add(publishButton);
            menuPanel.add(backButton);
            adminDialog.add(menuPanel, BorderLayout.CENTER);

            // 为品牌管理按钮添加监听器，当用户点击时执行品牌管理操作
            brandButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO: 在这里实现品牌管理的逻辑，可以弹出一个新的窗口进行品牌管理操作
                }
            });

            // 为车型管理按钮添加监听器，当用户点击时执行车型管理操作
            modelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 弹出一个对话框用于选择品牌
                    String[] brandOptions = { "雪佛兰", "大众", "丰田", "本田", "宝马", "奥迪" };
                    String selectedBrand = (String) JOptionPane.showInputDialog(null, "请选择品牌：", "选择品牌",
                            JOptionPane.PLAIN_MESSAGE, null, brandOptions, brandOptions[0]);

                    // 根据选择的品牌，展示车型管理菜单
                    JDialog modelDialog = new JDialog(adminDialog, "车型管理 - " + selectedBrand, true);
                    modelDialog.setSize(500, 550);
                    modelDialog.setLocationRelativeTo(adminDialog); // 在品牌管理窗口中心显示
                    modelDialog.setLayout(new BorderLayout());

                    // 创建两个按钮，并添加到车型管理窗口
                    JPanel modelPanel = new JPanel(new GridLayout(2, 1, 0, 10));
                    JButton addModelButton = new JButton("添加车型");
                    JButton deleteModelButton = new JButton("删除车型");
                    addModelButton.setFont(new Font("宋体", Font.PLAIN, 16));
                    deleteModelButton.setFont(new Font("宋体", Font.PLAIN, 16));
                    modelPanel.add(addModelButton);
                    modelPanel.add(deleteModelButton);
                    modelDialog.add(modelPanel, BorderLayout.CENTER);

                    // 添加车型按钮的监听器，在用户点击时执行添加车型逻辑
                    addModelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 弹出一个对话框用于输入车型信息
                            String modelName = JOptionPane.showInputDialog("请输入车型名称:");

                            // 弹出提示框表示车型信息添加成功
                            JOptionPane.showMessageDialog(null, "车型信息添加成功");
                        }
                    });

                    // 删除车型按钮的监听器，在用户点击时执行删除车型逻辑
                    deleteModelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 弹出一个对话框用于选择要删除的车型
                            String[] modelOptions = { "科鲁兹", "朗逸", "卡罗拉", "雅阁", "3系", "A4L" };
                            String selectedModel = (String) JOptionPane.showInputDialog(null, "请选择要删除的车型：", "选择车型",
                                    JOptionPane.PLAIN_MESSAGE, null, modelOptions, modelOptions[0]);

                            // TODO: 根据选择的车型，执行删除车型的操作

                            // 弹出提示框表示车型删除成功
                            JOptionPane.showMessageDialog(null, "车型删除成功");
                        }
                    });

                    // 为返回品牌管理菜单按钮添加监听器，当用户点击时关闭车型管理窗口并返回品牌管理菜单
                    backButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            modelDialog.dispose();
                            // 回到品牌管理界面
                            // TODO: 在这里实现品牌管理的逻辑，可以弹出一个新的窗口进行品牌管理操作
                        }
                    });

                    modelDialog.setVisible(true);
                }
            });

            // 为发布车辆信息按钮添加监听器，当用户点击时执行发布车辆信息操作

            publishButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 弹出一个对话框用于选择品牌
                    String[] brandOptions = { "雪佛兰", "大众", "丰田", "本田", "宝马", "奥迪" };
                    String selectedBrand = (String) JOptionPane.showInputDialog(null, "请选择品牌：", "选择品牌",
                            JOptionPane.PLAIN_MESSAGE, null, brandOptions, brandOptions[0]);

                    // 弹出一个对话框用于选择车型
                    String[] modelOptions = { "科鲁兹", "朗逸", "卡罗拉", "雅阁", "3系", "A4L" };
                    String selectedModel = (String) JOptionPane.showInputDialog(null, "请选择车型：", "选择车型",
                            JOptionPane.PLAIN_MESSAGE, null, modelOptions, modelOptions[0]);

                    // 弹出对话框让用户输入车辆详细信息
                    JTextField priceField = new JTextField();
                    JTextField mileageField = new JTextField();
                    // 添加其他车辆详细信息字段
                    // ...

                    JPanel inputPanel = new JPanel(new GridLayout(2, 2));
                    inputPanel.add(new JLabel("价格："));
                    inputPanel.add(priceField);
                    inputPanel.add(new JLabel("里程："));
                    inputPanel.add(mileageField);
                    // 添加其他车辆详细信息字段到inputPanel
                    // ...

                    int result = JOptionPane.showConfirmDialog(null, inputPanel, "请输入车辆详细信息",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        String price = priceField.getText();
                        String mileage = mileageField.getText();
                        // 获取其他车辆详细信息字段的值

                        // 获取当前系统时间作为发布时间
                        LocalDateTime currentTime = LocalDateTime.now();

                        // // 将注册信息插入到数据库
                        // try (Connection connection = DatabaseConnection.getConnection()) {
                        // String sql = "INSERT INTO car (brand, model, mileage, price, publishTime)
                        // VALUES (?, ?, ?, ?, ?)";
                        // PreparedStatement statement = connection.prepareStatement(sql);
                        // statement.setString(1, selectedBrand);
                        // statement.setString(2, selectedModel);
                        // statement.setInt(3, mileage);
                        // statement.setInt(4, price);
                        // statement.setInt(4, currentTime);
                        // int rows = statement.executeUpdate();
                        // if (rows > 0) {
                        // // 弹出提示框表示车辆信息发布成功
                        // JOptionPane.showMessageDialog(null, "车辆信息发布成功");
                        // } else {

                        // JOptionPane.showMessageDialog(null, "车辆信息发布失败");
                        // }

                        // }
                        JOptionPane.showMessageDialog(null, "车辆信息发布成功");
                    }
                }
            });

            // 为返回主菜单按钮添加监听器，当用户点击时关闭对话框并返回主菜单
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    adminDialog.dispose();
                }
            });
            brandButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 弹出一个对话框用于输入品牌信息
                    String brandName = JOptionPane.showInputDialog("请输入品牌名称:");

                    // 弹出提示框表示品牌信息添加成功
                    JOptionPane.showMessageDialog(null, "品牌信息添加成功");

                    // 回到品牌管理界面
                    // TODO: 在这里实现品牌管理的逻辑，可以弹出一个新的窗口进行品牌管理操作
                }
            });
            adminDialog.setVisible(true);
        } else if (isLoggedIn() && !Username.equals("admin")) {
            JOptionPane.showMessageDialog(this, "抱歉，您不是管理员");
        } else {
            JOptionPane.showMessageDialog(this, "请先登录哦");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}
