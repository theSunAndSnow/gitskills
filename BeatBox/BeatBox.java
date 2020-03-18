package chapter13.BeatBox;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.ArrayList;

public class BeatBox {

    JPanel mainPainl;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame frame;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
                                "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
                                "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
                                "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
                                "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};//每个设备对应不同得数字

    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }

    public void buildGUI() {
        frame = new JFrame("Cyber BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);//使JPanel的布局为BorderLayout
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //设定面板上摆设组件时的空白边缘

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);//一个轻量级容器：使用BoxLayout对象作为他的布局管理器

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        Box nameBox = new Box(BoxLayout.Y_AXIS);//音符名称对应的label。nameBox对应east方向的label
        for (int i = 0; i < 16; ++i) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);//buttonBox已经add了四个按钮（且）
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);//将background面板放在frame框架上

        GridLayout grid = new GridLayout(16, 16);/**/
        grid.setVgap(1);
        grid.setHgap(2);
        mainPainl = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPainl);//画板里再加个画板

        /**
         * 创建checkBox组，设定所有JCheckBox为false（未选中）并加到checkboxlist和mainPainl上
         */
        for (int i = 0; i < 256; ++i) {//共256个小方格
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPainl.add(c);
        }

        setUpMidi();
        frame.setBounds(50, 50, 300, 300);//设置框架界限
        frame.pack();//????
        frame.setVisible(true);
    }

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120); //????
        } catch (MidiUnavailableException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void buildTrackAndStart() {
        int[] trackList = null;

        sequence.deleteTrack(track); //清除掉旧的track并做一个新的track
        track = sequence.createTrack();

        for (int i = 0; i < 16; ++i) {
            trackList = new int[16];

            int key = instruments[i];//设定代表乐器的关键字
            /**
             * 每次遍历所有的checkBox，查找所有打勾选项
             */
            for (int j = 0; j < 16; ++j) {
                //checkboxList时ArrayList
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));//j列i行，用一维数组存二维元素位置
                if (jc.isSelected()) {//如果instruments[i]那一行中有一个框被打勾
                    trackList[j] = key;//trackList是个16*16数组，key可以重复进入trackList
                } else {
                    trackList[j] = 0;
                }
            }//关闭第二重循环
        }//关闭第一重循环

        makeTracks(trackList);//创建此乐器的事件并加到track上
        track.add(makeEvent(176, 1, 127, 0, 16));
        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException ex) {
            ex.printStackTrace();
        }
    }

    public class MyStartListener implements ActionListener {
        public void actionPerformed (ActionEvent a) {
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));
        }
    }

    public void makeTracks(int[] list) {

        for (int i = 0; i < 16; ++i) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return event;
    }

    public class MySendListener implements ActionListener {
        public void actionPerformed (ActionEvent event) {
            boolean[] checkBoxState = new boolean[256];

            int i = 0;
            for (JCheckBox check : checkboxList) {
                if (check.isSelected()) {
                    checkBoxState[i] = true;
                }
                ++i;
            }

            try {
                FileOutputStream fileStream = new FileOutputStream(
                        new File("E:/Java/HeadFirstJava/src/chapter13/BeatBox/Music.ser"));
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkBoxState);
            } catch (Exception ex) {
                System.out.println("can't open file");
                ex.printStackTrace();
            }
        }
    }// 结束内部类

    public class MyReadInListener implements ActionListener {
        public void actionPerformed (ActionEvent event) {
            boolean[] checkBoxState = null;

            try {
                FileInputStream fileStream = new FileInputStream(
                        new File("E:/Java/HeadFirstJava/src/chapter13/BeatBox/Music.ser"));
                ObjectInputStream is = new ObjectInputStream(fileStream);

                checkBoxState = (boolean[]) is.readObject();
            } catch (Exception ex) {
                System.out.println("can't open the file");
                ex.printStackTrace();
            }

            for (int i = 0; i < 256; ++i) {
                JCheckBox check = (JCheckBox) checkboxList.get(i);

                if (checkBoxState[i]) {
                    check.setSelected(true);
                } else {
                    check.setSelected(false);
                }
            }

            sequencer.stop();
            buildTrackAndStart(); //停止目前播放的节奏并使用复选框状态重新创建序列
        }
    }
}
