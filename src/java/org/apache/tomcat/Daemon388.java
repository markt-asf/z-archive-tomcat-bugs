package org.apache.tomcat;

import java.util.Set;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.StringMonitor;
import sun.jvmstat.monitor.VmIdentifier;

@SuppressWarnings("restriction")
public class Daemon388 {

    public static void main(String[] args) throws Exception {
        MonitoredHost host;
        Set<Integer> vms;
        host = MonitoredHost.getMonitoredHost(new HostIdentifier((String)null));
        vms = host.activeVms();

        for (Integer vmid: vms) {
            String pid = vmid.toString();
            String name = pid;      // default to pid if name not available
            MonitoredVm mvm = null;
            try {
                mvm = host.getMonitoredVm(new VmIdentifier(pid));
                try {
                    // use the command line as the display name
                    name = ((StringMonitor) mvm.findByName("sun.rt.javaCommand")).stringValue();
                } catch (Exception e) {
                }

                System.out.println(pid + " " + name);
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath)t;
                }
            } finally {
                if (mvm != null) {
                    mvm.detach();
                }
            }
        }
    }
}
