package com.docd.purefm.dialogs;

import org.apache.commons.io.FileUtils;

import com.docd.purefm.Environment;
import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandLineUtils;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.StatFsUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StatFs;
import android.view.View;
import android.widget.TextView;

public final class PartitionInfoDialog extends DialogFragment {
    
    public static DialogFragment instantiate(final GenericFile file) {
        final Bundle args = new Bundle();
        args.putSerializable(Extras.EXTRA_FILE, file);
        
        final PartitionInfoDialog d = new PartitionInfoDialog();
        d.setArguments(args);
        return d;
    }
    
    private GenericFile file;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle args = this.getArguments();
        this.file = (GenericFile) args.getSerializable(Extras.EXTRA_FILE);
    }
    
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setIcon(R.drawable.action_info);
        builder.setTitle(R.string.menu_partition);
        builder.setView(this.getInfoView());
        builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        return builder.create();
        
    }
    
    public View getInfoView() {
        final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_partition_info, null);
        final TextView title = (TextView) v.findViewById(R.id.location);
        String path = file.getCanonicalPath();
        if (path == null) {
            path = file.getAbsolutePath();
        }
        title.setText(path);
        
        final TextView fs = (TextView) v.findViewById(R.id.filesystem);
        String fsType = null;
        if (Environment.hasBusybox) {
            fsType = CommandLineUtils.getFSType(ShellHolder.getShell(), file.toFile());
        }
        
        if (fsType == null) {
            v.findViewById(R.id.filesystem_row).setVisibility(View.GONE);
        } else {
            fs.setText(fsType);
        }

        final StatFs stat = new StatFs(path);
        final long valueTotal = StatFsUtils.getBlockCountLong(stat) * StatFsUtils.getBlockSizeLong(stat);
        final long valueAvail = StatFsUtils.getAvailableBlocksLong(stat) * StatFsUtils.getBlockSizeLong(stat);
        final long valueUsed = valueTotal - valueAvail;
        
        final TextView total = (TextView) v.findViewById(R.id.total);
        if (valueTotal != 0L) {
            total.setText(FileUtils.byteCountToDisplaySize(valueTotal));
        }
        
        final TextView block = (TextView) v.findViewById(R.id.block_size);
        final long blockSize = StatFsUtils.getBlockSizeLong(stat);
        if (blockSize != 0L) {
            block.setText(Long.toString(blockSize));
        }
        
        final TextView free = (TextView) v.findViewById(R.id.free);
        if (valueTotal != 0L) {
            free.setText(FileUtils.byteCountToDisplaySize(
                    StatFsUtils.getFreeBlocksLong(stat) * StatFsUtils.getBlockSizeLong(stat)));
        }
        
        final TextView avail = (TextView) v.findViewById(R.id.available);
        if (valueTotal != 0L) {
            avail.setText(FileUtils.byteCountToDisplaySize(valueAvail));
        }
        
        final TextView used = (TextView) v.findViewById(R.id.used);
        if (valueTotal != 0L) {
            final StringBuilder usage = new StringBuilder();
            usage.append(FileUtils.byteCountToDisplaySize(valueUsed));
            usage.append(' ');
            usage.append('(');
            usage.append(valueUsed * 100L / valueTotal);
            usage.append('%');
            usage.append(')');
            used.setText(usage.toString());
        }
        
        return v;
    }
}
