package org.kafkalytic.plugin

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


class ProduceDialog(val project: Project, topic: String) : DialogWrapper(false), ChangeListener {
    private val LOG = Logger.getInstance(this::class.java)
    public final var PLACE_HOLDER : String = "key:value;";
    private lateinit var file: JTextField
    private lateinit var value: JTextArea
    private lateinit var key: JTextField
    private lateinit var headerKey: JTextField
    private lateinit var compression: ComboBox<String>
    private lateinit var radios: List<JRadioButton>
    override fun stateChanged(e: ChangeEvent?) {
        file.isEnabled = radios[0].isSelected
        value.isEnabled = radios[1].isSelected
    }
    init {
        setTitle("Configure Kafka producer for topic $topic")
        init();
        headerKey.addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent) {
                if (headerKey.getText().equals(PLACE_HOLDER)) {
                    headerKey.setText("")
                    headerKey.setForeground(UIUtil.getActiveTextColor())
                }
            }
            override fun focusLost(e: FocusEvent) {
                if (headerKey.getText().isEmpty()) {
                    headerKey.setForeground(UIUtil.getLabelDisabledForeground())
                    headerKey.setText(PLACE_HOLDER)
                }
            }
        })
    }

    override fun createCenterPanel(): JPanel {
        headerKey= JTextField()
        headerKey.preferredSize = Dimension(150, 24)
        headerKey = JTextField(PLACE_HOLDER)
        headerKey.setForeground(UIUtil.getActiveTextColor())


        key = JTextField()
        key.preferredSize = Dimension(150, 24)

        radios = arrayOf("Load from file ", "Text ").map { JRadioButton(it) }

        value = JTextArea(10, 43)
        value.lineWrap = true

        file = JTextField()
        file.preferredSize = Dimension(200, 24)
        val browse = JButton("Browse")
        browse.addActionListener{
            val fcd = FileChooserDescriptor(true, false, false, false, false, false)
            file.text = FileChooser.chooseFile(fcd, project, null)?.canonicalPath
        }

        compression = ComboBox(KAFKA_COMPRESSION_TYPES)

        var headerPanel = JPanel(GridLayout(2, 1))
        headerPanel.add(layoutLR(JBLabel("header: "), headerKey ))
        headerPanel.add(layoutLR(JBLabel("key of message"), key))

        val panel = JPanel(BorderLayout())
        panel.add(headerPanel, BorderLayout.NORTH)
        panel.add(JBLabel("Value"), BorderLayout.CENTER)
        panel.add(layoutUD(
            layoutLR(radios[1], JBScrollPane(value)),
            layoutLR(radios[0], layoutLR(file, browse)),
            layoutLR(JBLabel("Compression"), compression)), BorderLayout.SOUTH)

        val radioGroup = ButtonGroup()
        radios.forEach {
            radioGroup.add(it)
            it.addChangeListener(this)
        }
        radios[1].isSelected = true
        stateChanged(null)
        return panel
    }

    fun getHeaderKey() = headerKey.text
    fun getKey() = key.text
    fun getFile() = file.text
    fun getText() = value.text
    fun getMode() = radios[0].isSelected
    fun getCompression() = compression.selectedItem.toString()
}
